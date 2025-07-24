from flask import Flask, jsonify, request, abort
import os
import yaml
import subprocess
import libvirt
import requests
from dataclasses import dataclass, asdict

from service import Service

app = Flask(__name__)


@app.route("/api/healthcheck", methods=["GET"])
def healthcheck():
    return jsonify({"status": "ok"})


SERVICE_CATALOG_CONFIG_DIR = os.path.join(os.path.dirname(__file__), "services")

DNS_API_URL = "http://192.168.0.170:5000"

def load_service(name: str) -> Service:
    path = os.path.join(SERVICE_CATALOG_CONFIG_DIR, f"{name}.yaml")
    try:
        with open(path) as f:
            raw = yaml.safe_load(f)
    except FileNotFoundError:
        abort(404, f"Service '{name}' not found")
    # This will raise a TypeError if a required field is missing
    return Service(**raw)

@app.route("/api/services", methods=["GET"])
def get_services():
    services = []
    for filename in os.listdir(SERVICE_CATALOG_CONFIG_DIR):
        if filename.endswith((".yml", ".yaml")):
            service_name = filename.split(".")[0]
            services.append(asdict(load_service(service_name)))
    return jsonify(services)


@app.route("/api/services/<name>", methods=["GET"])
def get_service(name):
    svc = load_service(name)
    return jsonify(asdict(svc))

@app.route("/api/services/<name>/dns/provision", methods=["POST"])
def provision_dns_for_service(name):
    svc = load_service(name)

    data = request.get_json() or {}
    domain = data.get("domain", "").strip()

    if not domain:
        abort(400, "Missing domain in request body")

    ip = svc.ip
    port = svc.port

    if not ip or not port:
        abort(400, "This service does not have an associated IP or port, cannot provision a domain name")

    # Call the DNS API on the Raspberry Pi
    response = requests.post(f"{DNS_API_URL}/api/services/dns/provision", json={"domain": domain, "ip": ip, "port": port})
    if response.status_code == 200:
        return jsonify({"status": "ok", "domain": domain})
    else:
        return abort(response.status_code, "Something went wrong provisioning your domain name")




# Folder where your YAML configuration files reside.
VM_CONFIG_DIR = os.path.join(os.path.dirname(__file__), "vm_configs")

# Global dictionary to store VMs (keyed by VM name)
vms = {}

# Dictionary to keep track of noVNC server processes per VM
novnc_processes = {}

def load_vm_configs():
    """
    Load YAML config files from VM_CONFIG_DIR and update the global vms dictionary.
    """
    global vms
    vms = {}
    for filename in os.listdir(VM_CONFIG_DIR):
        if filename.endswith((".yml", ".yaml")):
            path = os.path.join(VM_CONFIG_DIR, filename)
            with open(path, "r") as f:
                config = yaml.safe_load(f)
                name = config.get("name")
                if name:
                    vms[name] = config

@app.route("/api/vms/reload", methods=["POST"])
def reload_vms():
    """Reload the YAML configs."""
    load_vm_configs()
    return jsonify({"status": "Sucess"}), 200

@app.route("/api/vms", methods=["GET"])
def get_vms():
    """Return the list of VMs (only those defined in the YAML files)."""
    return jsonify(list(vms.values()))

@app.route("/api/vms/<vm_name>", methods=["GET"])
def get_vm(vm_name):
    """Return detailed info for a particular VM."""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    return jsonify(vm)

@app.route("/api/vms/<vm_name>/status", methods=["GET"])
def get_vm_status(vm_name):
    """Return the current power state of a VM using libvirt."""
    if vm_name not in vms:
        return jsonify({"error": "VM not found"}), 404

    try:
        conn = libvirt.open("qemu:///system")
        domain = conn.lookupByName(vm_name)

        state, reason = domain.state()

        # Map libvirt states to readable strings
        libvirt_states = {
            libvirt.VIR_DOMAIN_NOSTATE: "no state",
            libvirt.VIR_DOMAIN_RUNNING: "running",
            libvirt.VIR_DOMAIN_BLOCKED: "blocked",
            libvirt.VIR_DOMAIN_PAUSED: "paused",
            libvirt.VIR_DOMAIN_SHUTDOWN: "shutting down",
            libvirt.VIR_DOMAIN_SHUTOFF: "shut off",
            libvirt.VIR_DOMAIN_CRASHED: "crashed",
            libvirt.VIR_DOMAIN_PMSUSPENDED: "suspended",
        }

        conn.close()

        return jsonify({
            "vm_name": vm_name,
            "state": libvirt_states.get(state, "unknown"),
            "raw_state_code": state,
            "raw_reason_code": reason
        })
    except libvirt.libvirtError as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/vms/<vm_name>/start", methods=["POST"])
def start_vm(vm_name):
    """Start the VM using libvirt bindings."""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    try:
        conn = libvirt.open("qemu:///system")
        domain = conn.lookupByName(vm_name)
        if domain is None:
            return jsonify({"error": "Domain not found"}), 404
        if not domain.isActive():
            domain.create()  # This starts the VM
        conn.close()
        return jsonify({"status": "VM started", "vm": vm})
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    
@app.route("/api/vms/<vm_name>/shutdown", methods=["POST"])
def shutdown_vm(vm_name):
    """Shut down the VM using libvirt bindings."""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    try:
        conn = libvirt.open("qemu:///system")
        domain = conn.lookupByName(vm_name)
        if domain is None:
            return jsonify({"error": "Domain not found"}), 404
        if domain.isActive():
            domain.shutdown()  # Graceful shutdown
        conn.close()
        return jsonify({"status": "Shutdown signal sent", "vm": vm})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/vms/<vm_name>/force-shutdown", methods=["POST"])
def force_shutdown_vm(vm_name):
    """Forcefully shut down the VM using libvirt bindings."""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    try:
        conn = libvirt.open("qemu:///system")
        domain = conn.lookupByName(vm_name)
        if domain is None:
            return jsonify({"error": "Domain not found"}), 404
        if domain.isActive():
            domain.destroy()  # Force shutdown (immediate power off)
        conn.close()
        return jsonify({"status": "VM forcefully shut down", "vm": vm})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/vms/<vm_name>/novnc/status", methods=["GET"])
def get_novnc_status(vm_name):
    """Return the current state of the noVNC server attached to a VM"""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    if vm_name in novnc_processes:
        return jsonify({"status": "running"})
    else:
        return jsonify({"status": "stopped"})

@app.route("/api/vms/<vm_name>/novnc/start", methods=["POST"])
def start_novnc(vm_name):
    """Start the noVNC server for the given VM."""
    
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    if vm_name in novnc_processes:
        return jsonify({"error": "noVNC server already running"}), 400
    try:
        # Use configuration from the VM's YAML.
        novnc_port = vm["novnc_port"]
        vnc_target  = f"localhost:{vm['vm_port']}"
        print(["/snap/bin/novnc", "--listen", str(novnc_port), "--vnc", vnc_target])
        # Start the noVNC server process.
        process = subprocess.Popen(
            ["/snap/bin/novnc", "--listen", str(novnc_port), "--vnc", vnc_target],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        novnc_processes[vm_name] = process
        vm["vnc_status"] = "running"
        return jsonify({"status": "noVNC server started", "vm": vm})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/vms/<vm_name>/novnc/stop", methods=["POST"])
def stop_novnc(vm_name):
    """Stop the noVNC server for the given VM."""
    vm = vms.get(vm_name)
    if not vm:
        return jsonify({"error": "VM not found"}), 404
    process = novnc_processes.get(vm_name)
    if not process:
        return jsonify({"error": "noVNC server not running"}), 400
    try:
        process.terminate()
        process.wait(timeout=5)
        novnc_processes.pop(vm_name, None)
        vm["vnc_status"] = "stopped"
        return jsonify({"status": "noVNC server stopped", "vm": vm})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    # Run on all interfaces on port 5000.
    # Load VM configs at startup.
    load_vm_configs()
    app.run(host="0.0.0.0", port=5000)
