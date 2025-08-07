'use client';

import { useTransition, useState } from 'react';
import { Button } from '../ui/button';
import { useRouter } from 'next/navigation';
import {
    getVMStatus,
    shutdownVirtualMachine,
    startVirtualMachine,
    VirtualMachine
} from '@/lib/vm';

interface VMProps {
    vm: VirtualMachine;
}

export default function VMStartStopButton({ vm }: VMProps) {
    const [action, setAction] = useState<'starting' | 'stopping' | null>(null);
    const [vmStatus, setVmStatus] = useState(vm.vm_status);
    const [isPending, startTransition] = useTransition();
    const router = useRouter();

    const delay = (ms: number) => new Promise((res) => setTimeout(res, ms));

    // Poll until status !== initialStatus, or maxTries exhausted
    const pollVmStatus = async (initialStatus: string) => {
        const maxTries = 10;
        for (let i = 0; i < maxTries; i++) {
            try {
                const status = await getVMStatus(vm.name);
                console.log('Polled status:', status);
                setVmStatus(status);

                if (status !== initialStatus) {
                    break;
                }
            } catch (e) {
                console.error('Error polling VM status:', e);
                break;
            }
            await delay(2000);
        }

        setAction(null);
        // once done, re-fetch data on the page
        startTransition(() => {
            router.refresh();
        });
    };

    const handleStartStop = async () => {
        const initial = vmStatus;

        if (initial === 'VIR_DOMAIN_RUNNING') {
            // stop
            setAction('stopping');
            try {
                const res = await shutdownVirtualMachine(vm.name);
                console.log('Shutdown API:', res.message);
            } catch (e) {
                console.error('Shutdown failed:', e);
                setAction(null);
                return;
            }
        } else {
            // start
            setAction('starting');
            try {
                const res = await startVirtualMachine(vm.name);
                console.log('Start API:', res.message);
            } catch (e) {
                console.error('Start failed:', e);
                setAction(null);
                return;
            }
        }

        // in either case, begin polling
        pollVmStatus(initial);
    };

    // Determine label + disabled state
    const isActing = action !== null;
    let label: string;
    if (isActing) {
        label = action === 'stopping' ? 'Shutting down...' : 'Starting...';
    } else {
        label =
            vmStatus === 'VIR_DOMAIN_RUNNING'
                ? 'Shut down'
                : 'Start';
    }

    return (
        <Button
            onClick={handleStartStop}
            disabled={isPending || isActing}
            className="font-bold"
        >
            {label}
        </Button>
    );
}
