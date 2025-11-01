import re
import ipaddress
import subprocess
import yaml
import docker

from flask import Flask, request, jsonify, abort

app = Flask(__name__)

NGINX_CONFIG_PATH  = "/app/config/default.conf"
NGINX_CONTAINER    = "nginx-internal"  # container name or ID

def validate_ip(ip_str: str) -> bool:
    try:
        ipaddress.ip_address(ip_str)
        return True
    except ValueError:
        return False

def validate_port(port) -> bool:
	return isinstance(port, int) and 1 <= port <= 65535

def validate_domain(domain: str) -> bool:
    # simple check: letters, numbers, dash, dot
    return bool(re.match(r'^[A-Za-z0-9.-]+$', domain))

def test_and_write_config(old_conf: str, new_conf: str):
    if old_conf is None or new_conf is None:
        return -1, "configs cannot be empty"

	# Write out and test
    with open(NGINX_CONFIG_PATH, "w") as f:
        f.write(new_conf)

    client = docker.from_env()
    try:
        nginx = client.containers.get(NGINX_CONTAINER)
    except docker.errors.NotFound:
        return -1, "nginx container not found"

    # Test the config
    test = nginx.exec_run("nginx -t", stdout=True, stderr=True)
    exit_code = test.exit_code
    output    = test.output.decode(errors="ignore")

    if exit_code != 0:
        # rollback on error
        #with open(NGINX_CONFIG_PATH, "w") as f:
        #    f.write(old_conf)
        app.logger.info(new_conf)
        return -1, "nginx config is incorrect"

    reload = nginx.exec_run("nginx -s reload", stdout=True, stderr=True)
    if reload.exit_code != 0:
        return -1, "nginx config reload failed"
       
    return 0, "success"

@app.route("/api/healthcheck", methods=["GET"])
def healthcheck():
	return jsonify({"status": "healthy"})

@app.route("/api/services/dns", methods=["POST"])
def provision_domain():
    data = request.get_json() or {}
    
    domain = data.get("domain", "").strip()
    ip   = data.get("ip", "").strip()
    port = data.get("port", "")

    if not domain or not validate_domain(domain):
        abort(400, "Missing or invalid 'domain' in request body")
    if not ip or not port:
        abort(400, "Service missing 'ip' or 'port'")
    if not validate_ip(ip):
        abort(400, f"Invalid IP address: {ip}")
    if not validate_port(port):
        abort(400, f"Invalid port: {port}")

    # 1) Read existing Nginx config
    with open(NGINX_CONFIG_PATH) as f:
        old_conf = f.read()

    new_conf = old_conf
    # 2) Look for an existing server block by matching proxy_pass ip:port
    block_re = re.compile(
        r"(server\s*\{[^}]*proxy_pass\s+http://"
        + re.escape(ip) + ":" + str(port)
        + r";[^}]*\})",
        re.DOTALL
    )
    m = block_re.search(old_conf)

    if m:
        print(f"Found existing block with {ip}:{port}, updating domain name only")
        # update only the server_name line
        block = m.group(1)
        updated = re.sub(
            r"server_name\s+[^\s;]+;",
            f"server_name {domain};",
            block
        )
        new_conf = old_conf.replace(block, updated)
    else:
        # append a fresh server block
        print(f"No block found with {ip}:{port}, adding a new one")
        new_block = f"""

server {{
    listen 80;
    server_name {domain};

    # --- Proxy to upstream (supports WebSockets) ---
    location / {{
        proxy_pass http://{ip}:{port};

        # Required for WebSockets
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;

        # Forward original host and client info
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Helpful for long-lived WS connections
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;

        # Avoid buffering WS frames
        proxy_buffering off;
    }}
}}
"""
        new_conf = old_conf.rstrip() + "\n" + new_block

    err_code, message = test_and_write_config(old_conf, new_conf)
    if err_code == 0:
        return jsonify({"status": "ok"})
    else:
        return jsonify({"error": message}), 500
    

@app.route("/api/services/dns/delete", methods=["POST"])
def delete_domain():
    data = request.get_json() or {}
    
    ip   = data.get("ip", "").strip()
    port = data.get("port", "")

    if not ip or not port:
        abort(400, "Service missing 'ip' or 'port'")
    if not validate_ip(ip):
        abort(400, f"Invalid IP address: {ip}")
    if not validate_port(port):
        abort(400, f"Invalid port: {port}")

    # 1) Read existing Nginx config
    with open(NGINX_CONFIG_PATH) as f:
        old_conf = f.read()

    # Regex to find the entire server block that proxies to ip:port
    block_re = re.compile(
        r"\n*server\s*\{[^}]*proxy_pass\s+http://"
        + re.escape(ip) + ":" + str(port)
        + r";[^}]*\}\n*",
        re.DOTALL
    )
    match = block_re.search(old_conf)
    if not match:
        return abort(404, f"No nginx block found for {ip}:{port}")

	# Remove that block
    new_conf = old_conf[:match.start()] + old_conf[match.end() + 1:]

    err_code, message = test_and_write_config(old_conf, new_conf)
    if err_code == 0:
        return jsonify({"status": "ok"})
    else:
        return jsonify({"error": message}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

