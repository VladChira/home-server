#!/usr/bin/env python3
import os
import sys
import time
import datetime
import shutil
import docker

# -- CONFIGURATION --
# Local world directory (expanded from ~)
WORLD_DIR = os.path.expanduser("~/services/minecraft/mainserver/world")
# Base backup directory
BACKUP_BASE_DIR = "/mnt/ssd/minecraft_backups"
DAILY_DIR = os.path.join(BACKUP_BASE_DIR, "daily")
WEEKLY_DIR = os.path.join(BACKUP_BASE_DIR, "weekly")

CONTAINER_NAME = "mainmcserver"

def announce_backup(container):
    """
    Uses rcon-cli inside the container to send a /say command via stdin.
    This creates an exec instance with stdin enabled and sends the command to its input.
    """
    try:
        # Create an exec instance running rcon-cli interactively.
        exec_instance = container.client.api.exec_create(
            container.id,
            cmd="rcon-cli",
            stdin=True,
            tty=False
        )
        # Start the exec instance and get a socket for communication.
        sock = container.client.api.exec_start(exec_instance, tty=False, socket=True)
        # Build the command; ensure a newline so the command is processed.
        command = "/say Server stopping in 30 seconds for daily backup...\n"
        # Send the command to the container’s rcon-cli via the socket.
        sock._sock.send(command.encode("utf-8"))
        # Optionally, you can read output from sock._sock.recv(1024) if needed.
        sock.close()
        print("Announcement sent via rcon-cli.")
    except Exception as e:
        print("Error sending announcement via rcon-cli:", e)

def container_is_stopped(container):
    if container.status != "running":
        return True
    return False

def stop_server(container):
    """
    Stops the Minecraft server container.
    """
    print("Stopping container...")
    try:
        container.stop(timeout=30)
        print("Container stopped.")
    except Exception as e:
        print("Error stopping container:", e)
        sys.exit(1)

def start_server(container):
    """
    Starts the Minecraft server container.
    """
    print("Starting container...")
    try:
        container.start()
        print("Container started.")
    except Exception as e:
        print("Error starting container:", e)
        sys.exit(1)

def create_zip_backup(src_dir, dest_zip_path):
    """
    Creates a zip archive from the source directory.
    The dest_zip_path should be the full path to the zip file (ending in .zip).
    Returns the path to the created archive.
    """
    # Remove the .zip extension to get the base name for make_archive
    base_name = dest_zip_path[:-4]
    try:
        archive_path = shutil.make_archive(base_name, 'zip', src_dir)
        print(f"Backup archive created at: {archive_path}")
        return archive_path
    except Exception as e:
        print("Error creating zip backup:", e)
        sys.exit(1)

def cleanup_old_backups(backup_dir, max_files=3):
    """
    In the specified backup_dir, remove the oldest zip files if there are
    more than max_files.
    """
    try:
        backups = [os.path.join(backup_dir, f)
                   for f in os.listdir(backup_dir)
                   if f.endswith('.zip')]
        # Sort backups by creation time (oldest first)
        backups.sort(key=lambda f: os.path.getctime(f))
        while len(backups) > max_files:
            oldest = backups.pop(0)
            os.remove(oldest)
            print("Deleted old backup:", oldest)
    except Exception as e:
        print("Error cleaning up backups in", backup_dir, ":", e)

def main():
    # Ensure backup directories exist
    os.makedirs(DAILY_DIR, exist_ok=True)
    os.makedirs(WEEKLY_DIR, exist_ok=True)

    # Connect to Docker and get the container
    client = docker.from_env()
    try:
        container = client.containers.get(CONTAINER_NAME)
    except docker.errors.NotFound:
        print(f"Container '{CONTAINER_NAME}' not found.")
        sys.exit(1)

    if container_is_stopped(container):
        print('Container is stopped, skipping backup')
        sys.exit(0)

    # Announce backup and wait 60 seconds for players to prepare
    announce_backup(container)
    print("Waiting 30 seconds for players to prepare...")
    time.sleep(30)

    # Stop the server before backup
    stop_server(container)

    # Create a timestamp and build the daily backup file name and path
    timestamp = datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    daily_backup_filename = f"daily_{timestamp}.zip"
    daily_backup_path = os.path.join(DAILY_DIR, daily_backup_filename)

    # Create daily backup archive
    print("Creating daily backup...")
    create_zip_backup(WORLD_DIR, daily_backup_path)

    # If today is Sunday (weekday() == 6), perform a weekly backup as well.
    if datetime.datetime.now().weekday() == 6:
        weekly_backup_filename = f"weekly_{timestamp}.zip"
        weekly_backup_path = os.path.join(WEEKLY_DIR, weekly_backup_filename)
        # Instead of taking another full backup, copy the daily backup.
        shutil.copy2(daily_backup_path, weekly_backup_path)
        print("Weekly backup created at:", weekly_backup_path)
        # Clean up old weekly backups
        cleanup_old_backups(WEEKLY_DIR, max_files=3)

    # Restart the Minecraft server container
    start_server(container)

    # Clean up old daily backups
    cleanup_old_backups(DAILY_DIR, max_files=3)

if __name__ == "__main__":
    main()
