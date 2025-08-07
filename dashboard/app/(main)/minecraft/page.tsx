"use client"

import { Button } from "@/components/ui/button";

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
import React, { useEffect, useState } from "react";
import ServerControlSwitch from "../../../components/minecraft/ServerControlSwitch";
import { getMinecraftServers, MinecraftServer } from "@/lib/minecraft";

export default function ServersPage() {
  const [error, setError] = useState();
  const [isLoading, setIsLoading] = useState(false);
  const [servers, setServers] = useState<MinecraftServer[]>([]);

  useEffect(() => {
    const fetchServers = async () => {
      setIsLoading(true);
      try {
        const servers = await getMinecraftServers();
        setServers(servers);
      } catch (e: any) {
        setError(e);
      } finally {
        setIsLoading(false);
      }
    }
    fetchServers();
  }, [])

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
                  <ServerControlSwitch serverName={server.name} defaultChecked={server.status === "running"} />
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
