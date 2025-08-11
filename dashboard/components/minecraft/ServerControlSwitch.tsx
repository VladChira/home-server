"use client";

import { Switch } from "@/components/ui/switch";
import { startMinecraftServer, stopMinecraftServer } from "@/lib/minecraft";
import { useMutation, useQueryClient } from "@tanstack/react-query";

interface ServerControlSwitchProps {
  serverName: string;
  defaultChecked: boolean;
}

export default function ServerControlSwitch({ serverName, defaultChecked }: ServerControlSwitchProps) {

  const qc = useQueryClient();

  const toggle = useMutation({
    mutationFn: async (checked: boolean) => {
      return checked ? startMinecraftServer(serverName) : stopMinecraftServer(serverName);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['minecraft'], refetchType: 'active' });
    },
  });

  return (
    <Switch
      id={serverName}
      defaultChecked={defaultChecked}
      onCheckedChange={(checked) => toggle.mutate(checked)}
    />
  );
}
