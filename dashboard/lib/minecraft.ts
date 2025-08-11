"use client"

import { useQuery } from "@tanstack/react-query";

export type MinecraftServer = {
    name: string,
    id: string,
    status: string
}

const API_CALL_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_API_URL;

export async function getMinecraftServers(): Promise<MinecraftServer[]> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/minecraft`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            console.error("Failed to fetch Minecraft servers");
            return [];
        }

        return await res.json() as MinecraftServer[];

    } catch (err) {
        console.error("Error while fetching Minecraft servers: ", err);
        return [];
    }
}

export async function startMinecraftServer(server_name: string): Promise<{ success: boolean; message: string }> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/minecraft/${server_name}/start`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            console.error("Failed to start Minecraft servers");
            return {
                success: false,
                message: `Server failed to start`,
            };
        }

        return {
            success: true,
            message: `Server started sucessfully`,
        };

    } catch (err) {
        console.error("Error while starting Minecraft server: ", err);
        return {
            success: false,
            message: `Server failed to start`,
        };
    }
}

export async function stopMinecraftServer(server_name: string): Promise<{ success: boolean; message: string }> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/minecraft/${server_name}/stop`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            console.error("Failed to stop Minecraft servers");
            return {
                success: false,
                message: `Server failed to stop`,
            };
        }

        return {
            success: true,
            message: `Server stopped sucessfully`,
        };

    } catch (err) {
        console.error("Error while stopping Minecraft server: ", err);
        return {
            success: false,
            message: `Server failed to stop`,
        };
    }
}

export function useMinecraftServers() {
    return useQuery<MinecraftServer[]>({
        queryKey: ['minecraft'],
        queryFn: getMinecraftServers,
        refetchOnWindowFocus: true
    });
}
