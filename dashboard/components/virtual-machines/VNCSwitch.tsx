"use client";

import { useRouter } from "next/navigation";
import { Switch } from "@/components/ui/switch";
import { startVNCServer, stopVNCServer, VirtualMachine } from "@/lib/vm";

interface VNCSwitchProps {
  vm: VirtualMachine;
  defaultChecked: boolean;
}

export default function VNCSwitch({ vm, defaultChecked }: VNCSwitchProps) {
  const router = useRouter();

  const handleChange = async (checked: boolean) => {
    if (checked) {
      await startVNCServer(vm.name);
    } else {
      await stopVNCServer(vm.name);
    }
    // Refresh the current route to fetch updated data
    router.refresh();
  };

  return (
    <Switch
      id={vm.name}
      defaultChecked={defaultChecked}
      onCheckedChange={handleChange}
    />
  );
}
