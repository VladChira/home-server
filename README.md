# My Home Server Setup

![Architecture Diagram](images/architecture.png)

With what initially started as a simple set of docker-compose files for a few containers, my home server has grown into quite a comprehensive setup, complete with my own custom tools and APIs. This repo is partially for showcasing this project and partially serving as a guide for myself if I ever were to re-deploy.

## Table of contents

1. [Overview](#overview)
2. [Hardware](#hardware)  
3. [Architecture](#architecture)   
4. [Stacks & Services](#stacks--services)   
   - [Ubuntu Host & KVM VMs](#ubuntu-host--kvm-vms)  
   - [Remote‑Access Stack](#remote-access-stack)  
   - [Media‑Server Stack](#media-server-stack)  
   - [Home Assistant Stack](#home-assistant-stack) 
   - [Monitoring Stack](#monitoring-stack)  
   - [Minecraft Backup Service](#minecraft-backup-service)   
   - [Portainer & Server Manager](#portainer--server-manager)  
   - [Service Catalog](#service-catalog)
   - [Raspberry Pi](#raspberry-pi-edge) 
5. [Configuration & Secrets](#configuration--secrets)  
6. [Backups](#backups)  
7. [AI Speech to Action Pipeline](#ai-speech-to-action-pipeline)
8. [The future](#the--future)


## Overview
I initially bought a Raspbery Pi 4 to serve as my home server in order to self-host and have fun. As I tinkered more and more, I was pushing it to the limits so I decided to upgrade. My goals now, besides actually using the services I deploy, are to learn as much as possible about infrastructure, microservices, sysadmin, networking etc. Testing new technologies (for example migrating an API to Spring Boot to learn the framework) in a familiar environment has also proven to be a very fast way to learn in my case.

## Hardware

The hardware I am using for my home server is an Intel NUC 14 Pro with the following specs:
- Core Ultra 5 125H 4.5Ghz Meteor Lake, 4 Performance cores, 8 Efficiency cores, 2 Low Power cores, 18 total threads
- Arc Integrated Graphics
- 32GB DDR5 5600Mhz SODIMM memory
- 256GB NVMe SSD
- 1TB SATA SSD
- 2TB External HDD

I was pleasantly surprised to see that this hardware holds up really well even after adding so many virtual machines and containers. The integrated graphics are quite useful for faster-than-real-time simultaneous video transcoding in Jellyfin (see the Media Server stack). While more RAM is always useful, two VMs running at the same time can still use up to 8GB of RAM each and everything runs just fine. Storage-wise I kind of fumbled, I wish I had gotten a bigger NVMe SSD, 256GB runs out quite quickly, especially with Docker build and image cache.

## Architecture

Bare metal, the server is running ``Ubuntu Server 24.04.2 LTS``. Having no GUI was a little bit difficult at first, but has been very rewarding, as I can easily work my way around a Linux terminal.

On top of Ubuntu Server, I run two types of virtualizations: ``Kernel-based Virtual Machines (KVMs)`` and Docker containers. Some services are ran bare-metal (as ``systemctl`` services for example) if they require interaction with the host OS.

The docker containers are "informally" organized in stacks. I've tried to deploy all services pertaining to a larger category in a single ``docker-compose.yaml`` file, but I wish I used a better abstraction (see Portainer section below).

The Manager API tries to centralize all indirect interactions between services (i.e. situations where the user must intervene in the interaction), and a NextJS dashboard exposes these interactions in a single place. 

To keep track of the services I have deployed, I also wrote a super simple Software Catalog (more details in its own section).

## Stacks & Services

### Ubuntu Host & KVM VMs

#### Cockpit
A truly fantastic tool for managing the host OS is Cockpit. It's a web-based interface for Linux with a bunch of features that I use all the time. It centralizes system info like logs, storage, networking, services, accounts, software updates and more.

Where it really shines for me is in its ``libvirt`` integration. I can use Cockpit to provision Virtual Machines very fast and intuitively. I can assign storage volumes from pools, attach virtual network interfaces, create VM snapshots and so so much more.

![Cockpit VM](images/cockpit-vm.png)

#### VM networking

On the topic of VMs, it's worth talking about their network configuration. For my main VM, I attach a TAP device to the bridged physical network adapter, allowing the VM to receive a real IP address on my home network. It can thus access other services without having to pass through the VM NAT. For other VMs, I prefer to use a virtual network with NAT. Cockpit allows me to swap between the interfaces very easily.


#### VM remote connections
Another aspect here is the remote connection. The whole point of the virtual machine was for me to be able to use it from outside my home too. Unfortunately, the remote connection protocol, VNC, is just bad. It's not encrypted, and since it's not HTTPS-based, it forces me to port-forward one port per virtual machine.

Punching holes in my firewall is a fantastically bad idea. ~~Somewhat fortunately, there is a solution: noVNC is an HTML VNC client that connects to a ``websockify`` instance that translates the VNC protocol traffic into websocket traffic. Therefore, after I start a virtual machine, the VNC connection is active, but cannot be accessed until a noVNC/websockify server is also activated. The port that websockify proxies to is already inside a proxy block in nginx. Because I have so few VMs, it's good enough (for now at least) to simply manually add the proxy block to nginx config.~~

![VM control](images/vms.png)

~~While VMs can be started from Cockpit directly, starting noVNC is annoying because it's a shell command.~~ Moreover, if I give other people access to their own VMs on my server, they need a way to turn it on/off. The Manager API uses libvirt bindings to start/stop the virtual machine and also executes the shell commands needed to turn on/off the noVNC server, and the dashboard nicely displays these options. I never access Cockpit to start/stop my virtual machines, always the dashboard. It's very convenient.

The API stores in a mySQL DB a description of which VM maps to which port. For example:
```yaml
name: "main-endeavouros"
novnc_port: 6092
vm_port: 5901
base_path: main-endeavouros
```

~~Despite all this, the setup still suffers from lag sometimes.~~ The protocol is just plain bad, it's beyond my control. 

I've switched to RDP protocol instead of VNC, and ditching noVNC for Apache Guacamole. I should have done this a long time ago, it's much better this way. Apache Guacamole is proxied by nginx, but the user login does not happen anywhere. Instead, it's the Manager API that obtains the short-lived access token based on some very strong password that is never exposed. 

#### Steps to provision a new VM with RDP remote connection
1. From Cockpit, create a volume under a particular storage pool.
2. Still from Cockpit, create a VM using an ISO, then give it lots of RAM (can lower it later, faster install this way)
3. Using the VNC connection that exists, configure the VM by installing OpenSSH server and allowing ports 22/tcp and 3389/tcp in the firewall. Also install ``qemu-guest-agent`` and ``acpid`` if available. 
4. Install ``xrdp`` package. Might need to also run ``sudo adduser xrdp ssl-cert``
5. Test ssh connection
6. Run this bash command if on Ubuntu to copy over the session customization of default Ubuntu Gnome, otherwise you will get a very basic session. Similar commands might be needed for other desktop envs.
```bash
cat <<EOF > ~/.xsessionrc
export GNOME_SHELL_SESSION_MODE=ubuntu
export XDG_CURRENT_DESKTOP=ubuntu:GNOME
export XDG_CONFIG_DIRS=/etc/xdg/xdg-ubuntu:/etc/xdg
EOF
```
7. Shut down the VM, and run ``sudo virsh edit <vm_name>`` and delete the VNC ``<graphics>`` tag. Save and start the VM.
8. Connect to the VM via ssh and run ``sudo systemctl enable --now xrdp``
9. Run ``ip a`` and take note of the IP address.
10. Reboot.
11. In Guacamole, create a connection using the IP address and port 3389, enter credentials, then tick ``Disable Certificate``.
12. Test the connection, taking note of the guac ID in the URL. Create an entry in the Manager DB with that guac ID.
13. Check if it appears on the Dashboard and test. Done.
### Remote-Access Stack
A core philosophy I have for this server is that it's very important to minimize all attack surfaces that appear from allowing remote connections. But I do acknowledge that *some* ways to remotely connect to the server are needed. So here is what I do security-wise:

#### SSH with two-factor authentication
While completely removing password authentication is the correct way to do ssh, I have decided there might be some rare situations where I prefer password + Google Authenticator. It should still be almost as secure as key-only.

#### nginx
The strict requirement I have for the ingress nginx is no HTTP, under any circumstance. I also have a catch-all statement in the config that completely rejects the SSL handshake if the client does not provide the correct domain name. Connecting via IP only is not possible. SSL certificates are obtained from ``Let's encrypt`` using ``certbot``. DDNS is obtained from my ISP.

#### Wireguard
By design, most services are not exposed to the internet, but sometimes I do want to access them remotely. For that, I use Wireguard. Not much to say here, other that each peer has its own file in the config, so only allowed devices can connect. If I ever need to add another device as a peer, I can use my phone (with the Termius app) to SSH into the server and add it. 


#### fail2ban
Having a public active ssh connection on port 22 really gets you a lot of traffic from bots, so fail2ban just monitors for failed connection attempts and temporarily bans their IPs. I can check the banned IPs by running ``sudo fail2ban-client status sshd``.


### Media Server Stack
This stack is made up of 6 containers that manage my media library. The core service is Jellyfin, the media server and player, which mounts a directory structure that contains my movies and TV shows. I also use the *arr suite for media mangement: Radarr for movies, Sonarr for TV shows, Prowlarr as an index manager and Jellyseerr for media requests. The flow for requesting media is as follows:

``Request media from Jellyseerr --> API call to Radarr/Sonarr --> API call to Prowlarr --> API call to indexer and to qBitTorrent client.``


#### Directory structure
A specific directory structure is needed to take advantage of hardlinks and atomic moves. Since everything is a Docker container, we need to mount volumes in such a way that the containers understand they are looking at the same physical drive to allow hardlinks.
```
├── MediaLibrary
│   ├── Downloads
│   └── Media
│       ├── Movies
│       └── Shows
```
Radarr and Sonarr mount ``MediaLibrary/``, qBitTorrent mounts ``MediaLibrary/Downloads/``, Jellyfin mounts ``MediaLibrary/Media/Movies/`` and ``MediaLibrary/Media/Shows/``. This way Radarr and Sonarr see both the ``Downloads/`` folder and the ``Media/`` folder and can use hardlinks to avoid storing duplicates of files as they are transferred from ``Downloads/`` to ``Media/``.

#### Jellyfin external access
Jellyfin can run under a subpath, so nginx proxies the traffic under that subpath to the Jellyfin container.


### Home Assistant Stack
Home Assistant manages my smart lights and sensors and does automations. Wifi lights are easy enough to link to HA. For the Zigbee sensors, I use a USB dongle, then zigbee2mqtt in a container. 

### Monitoring Stack

#### LGTM stack

#### Alerts & Notifications with Gotify

### Minecraft backup service
This is a simple script running on a cronjob that zips the contents of a world and stores it somewhere. It only does it if the server is turned on.

### Server Manager

The Server Manager API is a service that interacts and centralizes features that I considered essential to be in one place. Particularly, features that I wanted to interact with from outside my home network, but could not justify exposing the entire service for security/privacy reasons. For example, starting/stopping the VMs would be done in Cockpit, but exposing that to the internet is a very bad idea. Instead, the manager API interacts directly with ``libvirt``. 

Another use is to allow other people with appropriate credentials to interact with my server, such as friends turning on/off the Minecraft server whenever they wish.

~~The API is currently written in Flask~~. I have fully migrated to a Spring Boot backend using JPA to interact with the mySQL database. For security I use Spring Security with JWT and RBAC, which is then consumed by my NextJS dashboard.

#### Dashboard
The dashboard consumes the Manager API. It is written in React with NextJS, Shadcn and Tanstack Query. With this stack it is very simple to scaffold the UI, and getting to something I consider "good enough for my home server" takes no time.

### Service Catalog
Having so many services deployed on various ports are hard to track. I often found myself forgetting which service is on which port and had to open Portainer or run ``docker ps -a`` to remember. I wanted a way to centralize them in a single place, namely my dashboard. So I wrote a super simple YAML-based service catalog. Each YAML file corresponds to a service and has details about, including ip, port, description, url and more. Then the manager API interacts with these YAML files. I will soon migrate away from a YAML-based storage to an actual MySQL database and use ORM to query it. Again, not because it's better than the YAML files, but for educational purposes. 

#### Local DNS provisioning
On the subject of forgetting ports, combining the software catalog with local DNS records is an even better solution. For example, remembering ``pihole.server.local`` is way easier than ``192.168.0.170:8080``. 
And while I do not have enough services to require programatically created DNS records, it was still a fun exercise. Here is how I did it:

In pihole I added ``*.server.local`` as a local DNS record pointing to my Raspberry Pi's IP address. Since the web UI does not allow wildcard records, I added it to the dnsmasq config (by bind mounting the config in the container).

Then, I deployed a separate nginx instance on the Raspberry Pi to act as the proxy. The nginx config has server blocks matching domain names to IP:port combo. 

The config is managed by a tiny Flask API with the following routes:
```
GET /api/services/dns
Request body: ip, port
```
This route returns the domain name provisioned for a particular IP:port combo in the request body by reading the nginx config. Returns ``404`` if there is no matched IP:port server block. 

```
POST /api/services/dns
Request body: ip, port, domain
```
This route edits the nginx config by either adding a new server block or using regex to capture an existing server block matching ip:port (if it exists) and editing it with the new dns domain requested.

Analog for ``DELETE /api/services/dns``, no domain needed in the request body. 

This API is called by the manager API, never directly, because that will cause discrepancies between the service catalog and reality. Same thing when it comes to manually editing the nginx config. 

## Configuration & Secrets
Secrets like passwords public domain names or keys are stored in .env files that are gitignored. Sample .env are provided. For situations where it's not possible to store .envs, the files themselves are gitignored and sample files are provided.

## Backups
Currently there are no backups strategies for either the containers nor the files outside of containers. Fingers crossed.

## AI Speech to Action Pipeline

### Overview
The AI-powered speech to action pipeline is a service that enables voice control over some of the things in my home, similar to Google Home or Amazon Alexa.
It is designed to be fully local, although it supports offloading to cloud AI models.

Example usage: ``Hey Jarvis, please turn off all my lights``.

The flow is as follows:
- Wake word detection. Using OpenWakeWord, either running on my Android phone or on my server directly
- Utterance after wake word is being sent to the Manager API as a .wav file
- OpenAI Whisper speech to text converts the utterance to text
- The text is fed to an LLM with MCP tool calling capabilities with Spring AI
- The LLM takes the required action and produces an output
- THe output is fed into a text to speech model, which is then played on a speaker

I have integrated many tools from different sources. The idea is for the Server Manager MCP server to be a *composer of tools*.

### Wake Word Detection
Wake word detection is done using OpenWakeWord, a free and open-source wakeword library that has great performance on tiny devices. Officially it is implemented in Python only, but I did find [a repository](https://github.com/hasanatlodhi/OpenwakewordforAndroid) that implemented it for Android, which works really well with a few changes:
- Replace hey nugget with hey jarvis pre-trained model
- Added way to run as foreground service to permanently record audio

Once wake word has been detected, the utterance is being recorded and saved to a .wav file. Then the file is uploaded to the Server Manager API and the rest of the pipeline ca proceed.

### Speech to Text
Once the following endpoint is called with an audio recording of the prompt:
```java
@PostMapping(path = "/command", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Map<String, String>> runVoiceCommand(@RequestPart("audio") MultipartFile audio) {
   // ...
}
```
The file is saved to disk, then the Whisper binary is called to produce the text. Models ``tiny`` and ``base`` seem to work good enough in normal settings. The point is to be faster than more accurate, since most typos can be worked around by the LLMs. 

### LLM Providers and Tool Calling


#### Home Assistant Tools

#### Task Scheduler

### Observability

### Text to Speech

### Model size constraints
Running an LLM locally provides an extra challenge, as I need both a lot of VRAM and a powerful GPU, of which I have neither. I can get away with running a small none-thinking model with tool-calling capabilities, such as ``qwen2.5:7b``. It seems to be the most consistent when using Spring AI, although it is poor at planning more complex chains of actions.

Moreover, to make it fast, I need to use very few tokens for the name, descriptions and results of the MCP tools, which is why I cannot simply rely on the Home Assistant MCP. The tools are too complicated for a tiny AI to correctly call. This is the reason I implemented my own tools on top of the HA API instead. I can control the amount of tokens I use in the prompts and MCP tools. 

For more complicated calls, I can simply call ``gpt-5-nano``, which is both fast and cheap.

### Mobile App


## The future
Here are a few things I'd love to implement:
- ~~Better Grafana Dashboard~~  DONE
- ~~Better alerting with Grafana and Gotify~~  DONE
- ~~Speech to action pipeline for AI assistant (integrate with Whisper, ChatGPT, MCP, Home Assistant and Manager API)~~  DONE
- Better remote connection to VMs
- Better Dashboard home page