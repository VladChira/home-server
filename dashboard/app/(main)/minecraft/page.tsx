export const dynamic = 'force-dynamic';

import { Button } from "@/components/ui/button";
import { listMinecraftServers, startServerAction, stopServerAction } from "@/app/actions/minecraftServerActions";

import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import React from "react";
import { Switch } from "@/components/ui/switch";
import ServerControlSwitch from "../../../components/minecraft/ServerControlSwitch";

type MinecraftServer = {
  id: string;
  name: string;
  port: number;
  status: string;
  details: string;
};

const servers: MinecraftServer[] = [
  {
    id: "4410bda",
    name: "Survival Server",
    port: 25565,
    status: "Online",
    details: "Welcome to the Survival Server! Explore, build, and survive.",
  },
  {
    id: "47fff35",
    name: "Creative Server",
    port: 25566,
    status: "Offline",
    details: "This is a Creative mode server with infinite resources.",
  },
  {
    id: "f28f5dd",
    name: "Modded Server",
    port: 25567,
    status: "Online",
    details: "Heavily modded server with custom items and biomes.",
  },
];

export default async function ServersPage() {
  const servers = await listMinecraftServers();

  return (
    <main className="p-1">
      <h1 className="tedivxt-2xl font-bold mb-5 mt-3">Minecraft Servers</h1>
      <div className="px-2">
        <Table className="bg-card border-1">
          <TableCaption>List of Minecraft servers</TableCaption>
          <TableHeader>
            <TableRow>
              <TableHead className="font-medium text-xl">Name</TableHead>
              <TableHead className="font-medium text-xl">Port</TableHead>
              <TableHead className="font-medium text-xl">Status</TableHead>
              <TableHead className="font-medium text-xl">Start/Stop</TableHead>
              <TableHead className="font-medium text-xl">Backup</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {servers.map((server) => (
              <TableRow key={server.id}>
                <TableCell className="font-medium">
                  <div className="flex flex-col">
                    <span className="font-bold text-lg">{server.name}</span>
                    <span className="text-muted-foreground">{server.id}</span>
                  </div>
                </TableCell>
                <TableCell className="text-base font-medium">N/A</TableCell>
                <TableCell className="text-base font-bold">
                  <div className="flex items-center gap-2">
                    <span
                      className={`inline-block w-3 h-3 rounded-full ${server.status === "running" ? "bg-green-500" : "bg-red-500"
                        }`}
                    />
                    <span>{server.status == "running" ? "Running" : "Stopped"}</span>
                  </div>
                </TableCell>
                <TableCell className="text-base font-medium">
                  <ServerControlSwitch serverId={server.id} defaultChecked={server.status === "running"} />
                </TableCell>
                <TableCell className="text-base font-medium">
                  <Button className="text-base font-medium">
                    Download
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </main >
  );
}
