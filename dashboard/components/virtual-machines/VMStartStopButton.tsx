'use client';

import { useTransition, useState } from 'react';
import { Button } from '../ui/button';
import { useRouter } from 'next/navigation';
import { getVMStatus, shutdownVirtualMachine, startVirtualMachine, VirtualMachine } from '@/lib/vm';

interface VMProps {
    vm: VirtualMachine
}

export default function VMStartStopButton({ vm }: VMProps) {
    const [isPending, startTransition] = useTransition();
    const [result, setResult] = useState<string | null>(null);
    const [isShuttingDown, setIsShuttingDown] = useState(false);
    const router = useRouter();

    const pollVmStatus = async () => {
        const maxTries = 10;
        const delay = (ms: number) => new Promise((res) => setTimeout(res, ms));

        for (let i = 0; i < maxTries; i++) {
            const status = await getVMStatus(vm.name);

            if (status !== 'running') {
                break;
            }

            await delay(2000);
        }

        setIsShuttingDown(false);
        router.refresh();
    };

    const handleStartStop = () => {
        startTransition(async () => {
            if (vm.vm_status === 'VIR_DOMAIN_RUNNING') {
                setIsShuttingDown(true);
                const res = await shutdownVirtualMachine(vm.name);
                setResult(res.message);
                pollVmStatus(); // start polling instead of refreshing immediately
            } else {
                const res = await startVirtualMachine(vm.name);
                setResult(res.message);
                router.refresh();
            }
        });
    };

    return (
        <Button onClick={handleStartStop} disabled={isPending || isShuttingDown} className="font-bold">
            {(isPending || isShuttingDown)
                ? (vm.vm_status === 'VIR_DOMAIN_RUNNING' ? 'Shutting down...' : 'Starting...')
                : vm.vm_status === 'VIR_DOMAIN_RUNNING'
                    ? 'Shut down'
                    : 'Start'}
        </Button>
    );
}

