"use client"

import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "./api";

export type DomainState =
    | 'VIR_DOMAIN_CRASHED'
    | 'VIR_DOMAIN_NOSTATE'
    | 'VIR_DOMAIN_RUNNING'
    | 'VIR_DOMAIN_SHUTDOWN'
    | 'VIR_DOMAIN_SHUTOFF';

export type VirtualMachine = {
    name: string;
    vm_status: DomainState | 'unknown';
    guac_id?: string;
};


export async function getVMStatus(vmName: string): Promise<string> {
    try {
        const res = await apiFetch('/vms/${vmName}/status', { method: 'GET' });

        if (!res.ok) {
            console.warn(`Failed to fetch VM status for ${vmName}`);
            return 'unknown';
        }

        const data = await res.json();
        return data.state || 'unknown';
    } catch (err) {
        console.error(`Error while fetching VM status for ${vmName}:`, err);
        return 'unknown';
    }
}

export async function getVirtualMachines(): Promise<VirtualMachine[]> {
    // Fetch all VM config entries
    const res = await apiFetch('/vms', { method: 'GET' });

    if (!res.ok) {
        console.error("Failed to fetch VMs:", res.statusText);
        throw new Error("Failed to fetch virtual machines");
    }

    const configVMs = await res.json();

    // For each VM, fetch its live status
    const enrichedVMs: VirtualMachine[] = await Promise.all(
        configVMs.map(async (vm: any): Promise<VirtualMachine> => {
            let vmStatus: DomainState | 'unknown' = 'unknown';

            try {
                const statusRes = await apiFetch(`/vms/${vm.name}/status`, { method: 'GET' });

                if (statusRes.ok) {
                    const statusData = await statusRes.json();
                    vmStatus = statusData.state || 'unknown';
                } else {
                    console.warn(`Could not fetch status for VM ${vm.name}: ${statusRes.statusText}`);
                }
            } catch (err) {
                console.error(`Error fetching status for VM ${vm.name}:`, err);
            }
            return {
                name: vm.name,
                vm_status: vmStatus,
                guac_id: vm.guacClientId,
            };
        })
    );
    return enrichedVMs;
}

export async function startVirtualMachine(vmName: string): Promise<{ success: boolean; message: string }> {
    try {
        const res = await apiFetch(`/vms/${vmName}/start`, { method: 'POST' });

        if (!res.ok) {
            const error = await res.json();
            return {
                success: false,
                message: error.error || 'Failed to start VM',
            };
        }
        return {
            success: true,
            message: `VM "${vmName}" started successfully`,
        };
    } catch (err: any) {
        return {
            success: false,
            message: err.message || 'Request failed',
        };
    }
}

export async function shutdownVirtualMachine(vmName: string): Promise<{ success: boolean; message: string }> {
    console.log("here");
    try {
        const res = await apiFetch(`/vms/${vmName}/shutdown`, { method: 'POST' });

        if (!res.ok) {
            const error = await res.json();
            return {
                success: false,
                message: error.error || 'Failed to shut down VM',
            };
        }

        return {
            success: true,
            message: `Shutdown signal sent to VM "${vmName}"`,
        };
    } catch (err: any) {
        return {
            success: false,
            message: err.message || 'Request failed',
        };
    }
}

export async function forceShutdownVirtualMachine(vmName: string): Promise<{ success: boolean; message: string }> {
    try {
        const res = await apiFetch(`/vms/${vmName}/force-shutdown`, { method: 'POST' });

        if (!res.ok) {
            const error = await res.json();
            return {
                success: false,
                message: error.error || 'Failed to force shutdown VM',
            };
        }

        return {
            success: true,
            message: `VM "${vmName}" was forcefully shut down`,
        };
    } catch (err: any) {
        return {
            success: false,
            message: err.message || 'Request failed',
        };
    }
}

export async function getGuacToken(): Promise<string> {
    try {
        const res = await apiFetch('/vms/guac-token', { method: 'GET' });

        if (!res.ok) {
            console.error("Failed to fetch Guacamole token:", res.statusText);
            throw new Error("Failed to fetch Guacamole token");
        }

        const data = await res.json();
        return data['guac-token'];
    } catch (err) {
        console.error("Error while fetching Guacamole token:", err);
        throw err;
    }
}

export function useVMs() {
    return useQuery<VirtualMachine[]>({
        queryKey: ['vms'],
        queryFn: getVirtualMachines,
        refetchOnWindowFocus: true
    });
}