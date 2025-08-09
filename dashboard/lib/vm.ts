"use client"

export type VirtualMachine = {
    name: string;
    vnc_status: string,
    vm_status: string,
    base_path: string;
};

const API_CALL_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_API_URL;

export async function getVMStatus(vmName: string): Promise<string> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/status`, {
            method: 'GET', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

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
    const token = localStorage.getItem('accessToken');
    // Fetch all VM config entries
    const res = await fetch(`${API_CALL_BASE_URL}/vms`, {
        method: 'GET', headers: {
            'Authorization': `Bearer ${token}`,
        },
    });

    if (!res.ok) {
        console.error("Failed to fetch VMs:", res.statusText);
        throw new Error("Failed to fetch virtual machines");
    }

    const configVMs = await res.json();

    // For each VM, fetch its live status
    const enrichedVMs: VirtualMachine[] = await Promise.all(
        configVMs.map(async (vm: any): Promise<VirtualMachine> => {
            let vmStatus = 'unknown';

            try {
                const statusRes = await fetch(`${API_CALL_BASE_URL}/vms/${vm.name}/status`, {
                    method: 'GET', headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (statusRes.ok) {
                    const statusData = await statusRes.json();
                    vmStatus = statusData.state || 'unknown';
                } else {
                    console.warn(`Could not fetch status for VM ${vm.name}: ${statusRes.statusText}`);
                }
            } catch (err) {
                console.error(`Error fetching status for VM ${vm.name}:`, err);
            }

            let vncStatus = 'unknown'
            try {
                const statusRes = await fetch(`${API_CALL_BASE_URL}/vms/${vm.name}/novnc/status`, {
                    method: 'GET', headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (statusRes.ok) {
                    const statusData = await statusRes.json();
                    vncStatus = statusData.status || 'unknown';
                } else {
                    console.warn(`Could not fetch status for noVNC server for VM ${vm.name}: ${statusRes.statusText}`);
                }
            } catch (err) {
                console.error(`Error fetching status for noVNC server for VM ${vm.name}:`, err);
            }

            return {
                name: vm.name,
                base_path: vm.base_path,
                vm_status: vmStatus,
                vnc_status: vncStatus,
            };
        })
    );
    return enrichedVMs;
}

export async function startVirtualMachine(vmName: string): Promise<{ success: boolean; message: string }> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/start`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

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
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/shutdown`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

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
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/force-shutdown`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

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

export async function startVNCServer(vmName: string): Promise<{ success: boolean; message: string }> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/novnc/start`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            const error = await res.json();
            return {
                success: false,
                message: error.error || 'Failed to start noVNC server for VM',
            };
        }

        return {
            success: true,
            message: `noVNC server successfully started`,
        };
    } catch (err: any) {
        return {
            success: false,
            message: err.message || 'Request failed',
        };
    }
}

export async function stopVNCServer(vmName: string): Promise<{ success: boolean; message: string }> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/vms/${vmName}/novnc/stop`, {
            method: 'POST', headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            const error = await res.json();
            return {
                success: false,
                message: error.error || 'Failed to stop noVNC server for VM',
            };
        }

        return {
            success: true,
            message: `noVNC server successfully stopped`,
        };
    } catch (err: any) {
        return {
            success: false,
            message: err.message || 'Request failed',
        };
    }
}

