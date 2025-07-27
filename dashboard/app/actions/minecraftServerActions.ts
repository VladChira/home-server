"use server";

import Docker from "dockerode";

export async function listMinecraftServers() {
    const docker = new Docker();
    try {
        // Get a list of all containers
        const containers = await docker.listContainers({ all: true });

        // Filter containers whose image name includes "minecraft-server"
        const minecraftContainers = containers.filter((container) => {
            return container.Image.includes("minecraft-server");
        });

        return (minecraftContainers.sort((a: Docker.ContainerInfo, b: Docker.ContainerInfo) => {
            return a.Id.localeCompare(b.Id) 
        })).map((container) => ({
            id: container.Id.slice(0, 12), // short_id

            name: container.Names[0].replace(/^\//, ""), // remove leading slash from name
            status: container.State,
        }));
    } catch (err) {
        throw new Error("Something went wrong: Failed to parse containers");
    }
}

export async function getServerStatus(serverId: string) {
    const docker = new Docker();
    try {
        const container = docker.getContainer(serverId);
        const info = await container.inspect();

        if (!info.Config.Image.includes("minecraft-server")) {
            throw new Error("Not a Minecraft server");
        }

        // Return the container's current status (e.g. "running", "exited", etc.)
        return info.State.Status;
    } catch (err) {
        throw new Error(
            "Something went wrong: The container with that ID does not exist or is not a Minecraft server"
        );
    }
}

export async function startServerAction(serverId: string) {
    const docker = new Docker();
    try {
        const container = docker.getContainer(serverId);
        const info = await container.inspect();

        // Check if the container's image name includes "minecraft-server"
        if (!info.Config.Image.includes("minecraft-server")) {
            throw new Error("Not a Minecraft server");
        }

        await container.start();
        return { message: `Server ${serverId} started successfully` };
    } catch (err: any) {
        throw new Error(err.message);
    }
}

export async function stopServerAction(serverId: string) {
    const docker = new Docker();
    try {
        const container = docker.getContainer(serverId);
        const info = await container.inspect();

        // Check if the container's image name includes "minecraft-server"
        if (!info.Config.Image.includes("minecraft-server")) {
            throw new Error("Not a Minecraft server");
        }

        await container.stop();
        return { message: `Server ${serverId} stopped successfully` };
    } catch (err: any) {
        throw new Error(err.message);
    }
}
