// components/ServerControlSwitch.tsx
"use client";

import { useRouter } from "next/navigation";
import { Switch } from "@/components/ui/switch";
import { startServerAction, stopServerAction } from "@/app/actions/minecraftServerActions";

interface ServerControlSwitchProps {
  serverId: string;
  defaultChecked: boolean;
}

export default function ServerControlSwitch({ serverId, defaultChecked }: ServerControlSwitchProps) {
  const router = useRouter();

  const handleChange = async (checked: boolean) => {
    if (checked) {
      await startServerAction(serverId);
    } else {
      await stopServerAction(serverId);
    }
    // Refresh the current route to fetch updated data
    router.refresh();
  };

  return (
    <Switch
      id={serverId}
      defaultChecked={defaultChecked}
      onCheckedChange={handleChange}
    />
  );
}
