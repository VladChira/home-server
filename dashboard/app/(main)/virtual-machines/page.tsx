"use client"
export const dynamic = 'force-dynamic';

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
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
import VMForceShutdownButton from "@/components/virtual-machines/VMForceShutdown";
import VMStartStopButton from "@/components/virtual-machines/VMStartStopButton";
import VNCSwitch from "@/components/virtual-machines/VNCSwitch";
import { getVirtualMachines, VirtualMachine } from "@/lib/vm";
import { AlertTriangle, ExternalLink, Terminal } from "lucide-react";
import { redirect } from "next/navigation";
import { useEffect, useState } from "react";



const virtualMachines: VirtualMachine[] = [
    {
        name: "denis-vm",
        vnc_status: "stopped",
        vm_status: "running",
        base_path: "denis"
    },
    {
        name: "main-ubuntu",
        vnc_status: "stopped",
        vm_status: "stopped",
        base_path: "main-ubuntu"
    },
    {
        name: "windows-vm",
        vnc_status: "stopped",
        vm_status: "running",
        base_path: "windows-vm"
    },
];

const VM_URL = process.env.NEXT_PUBLIC_VM_URL;

export default function VMsPage() {
    const [error, setError] = useState();
    const [isLoading, setIsLoading] = useState(false);
    const [vms, setVMs] = useState<VirtualMachine[]>([]);

    useEffect(() => {
        const fetchVMs = async () => {
            setIsLoading(true);
            try {
                const vms = await getVirtualMachines();
                setVMs(vms);
            } catch (e: any) {
                setError(e);
            } finally {
                setIsLoading(false);
            }
        }
        fetchVMs();
    }, [])

    if (isLoading)
        return <div>Loading...</div>

    return (
        <main className="p-1">
            <h1 className="tedivxt-2xl font-bold mb-5 mt-3">Configured Virtual Machines</h1>

            <div className="pt-3">
                <Alert>
                    <AlertTriangle className="h-6 w-6" />
                    <AlertTitle className="text-base">Warning</AlertTitle>
                    <AlertDescription>
                        <span className="font-bold">Do not turn off virtual machines that do not belong to you. If you do, I will find you...</span>
                    </AlertDescription>
                </Alert>
            </div>

            <div className="px-2 pt-5">
                <Table className="bg-card border-1">
                    <TableHeader>
                        <TableRow>
                            <TableHead className="font-medium text-xl">Name</TableHead>
                            <TableHead className="font-medium text-xl">VNC Status</TableHead>
                            <TableHead className="font-medium text-xl">VNC Server</TableHead>
                            <TableHead className="font-medium text-xl">VM Status</TableHead>
                            <TableHead className="font-medium text-xl">Virtual Machine</TableHead>
                        </TableRow>
                    </TableHeader>

                    <TableBody>
                        {vms.map((vm) => (
                            <TableRow key={vm.name}>
                                <TableCell className="font-medium">
                                    <span className="font-bold text-lg">{vm.name}</span>
                                </TableCell>
                                <TableCell className="text-base font-bold">
                                    <div className="flex items-center gap-2">
                                        <span
                                            className={`inline-block w-3 h-3 rounded-full ${vm.vnc_status === "running" ? "bg-green-500" : "bg-red-500"
                                                }`}
                                        />
                                        <span>{vm.vnc_status}</span>
                                    </div>
                                </TableCell>
                                <TableCell className="text-base font-medium">
                                    <div className="flex items-center gap-2">
                                        <VNCSwitch vm={vm} defaultChecked={vm.vnc_status == "running"} />
                                        <Button asChild className="bg-accent-foreground">
                                            <a
                                                href={`${VM_URL}/${vm.name}/vnc.html`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <ExternalLink />
                                            </a>
                                        </Button>

                                    </div>
                                </TableCell>
                                <TableCell className="text-base font-medium">
                                    <div className="flex items-center gap-2">
                                        <span
                                            className={`inline-block w-3 h-3 rounded-full ${vm.vm_status === "running" ? "bg-green-500" : "bg-red-500"
                                                }`}
                                        />
                                        <span>{vm.vm_status}</span>
                                    </div>
                                </TableCell>
                                <TableCell>
                                    <div className="flex items-center gap-3">
                                        <VMStartStopButton vm={vm} />
                                        {vm.vm_status === 'running' && (
                                            <VMForceShutdownButton vm={vm} />
                                        )}
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </div>

            <div className="pt-5">
                <Alert>
                    <Terminal className="h-4 w-4" />
                    <AlertTitle className="text-base">VNC Server Explanation</AlertTitle>
                    <AlertDescription>
                        The virtual machine natively exposes a VNC port for remote connection. However, it is not encrypted and requires a separate client. Which is why another service is needed to proxy the VNC connection to a web server. The VNC server must be turned on to be able to remote into a virtual machine. You may access the virtual machine by clicking on the button next to the VNC Server switch. For extra security, please turn off the VNC server once you are done using the virtual machine.
                    </AlertDescription>
                </Alert>
            </div>
        </main >
    );
}
