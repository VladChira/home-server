'use client';

import {
    getVMStatus,
    shutdownVirtualMachine,
    startVirtualMachine,
    VirtualMachine
} from '@/lib/vm';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Switch } from '../ui/switch';
import { Button } from '../ui/button';

interface VMProps {
    vm: VirtualMachine;
}

export default function VMStartStopButton({ vm }: VMProps) {

    const qc = useQueryClient();

    const running = vm.vm_status === "VIR_DOMAIN_RUNNING";

    const toggle = useMutation({
        mutationFn: async () => {
            return running ? shutdownVirtualMachine(vm.name) : startVirtualMachine(vm.name);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ['vms'], refetchType: 'active' });
        },
    });

    const label = toggle.isPending
        ? running
            ? 'Shutting down...'
            : 'Starting...'
        : running
            ? 'Shut down'
            : 'Start';

    return (
        <Button
            onClick={() => toggle.mutate()}
            disabled={toggle.isPending}
            className="font-bold"
        >
            {label}
        </Button>
    );
}
