"use client";

import { useRouter } from "next/navigation";
import { Switch } from "@/components/ui/switch";
import { startMinecraftServer, stopMinecraftServer } from "@/lib/minecraft";

interface ServerControlSwitchProps {
  serverName: string;
  defaultChecked: boolean;
}

export default function ServerControlSwitch({ serverName, defaultChecked }: ServerControlSwitchProps) {
  const router = useRouter();

  const handleChange = async (checked: boolean) => {
    if (checked) {
      await startMinecraftServer(serverName);
    } else {
      await stopMinecraftServer(serverName);
    }
    // Refresh the current route to fetch updated data
    router.refresh();
  };

  return (
    <Switch
      id={serverName}
      defaultChecked={defaultChecked}
      onCheckedChange={handleChange}
    />
  );
}
