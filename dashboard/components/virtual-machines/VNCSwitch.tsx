"use client";

import { useRouter } from "next/navigation";
import { Switch } from "@/components/ui/switch";
import { startVNCServer, stopVNCServer, VirtualMachine } from "@/lib/vm";
import { useMutation, useQueryClient } from "@tanstack/react-query";

interface VNCSwitchProps {
  vm: VirtualMachine;
  defaultChecked: boolean;
}

export default function VNCSwitch({ vm, defaultChecked }: VNCSwitchProps) {
  const qc = useQueryClient();

  const toggle = useMutation({
    mutationFn: async (checked: boolean) => {
      console.log(checked)
      return checked ? startVNCServer(vm.name) : stopVNCServer(vm.name);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['vms'], refetchType: 'active' });
    },
  });

  return (
    <Switch
      id={vm.name}
      disabled={toggle.isPending}
      defaultChecked={defaultChecked}
      onCheckedChange={(checked) => toggle.mutate(checked)}
    />
  );
}
