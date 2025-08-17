import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "./api";

export type MemoryPoint = {
    time: string;
    value: number;
};

function queryResultToPoints(values: [number, string][]): MemoryPoint[] {
    const result: MemoryPoint[] = values.map(([ts, val]: [number, string]) => {
        const date = new Date(ts * 1000);
        const label = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        return {
            time: label,
            value: parseFloat(val),
        };
    });
    return result;
}

export async function getRAMUsageData(sinceSeconds: number, stepSeconds: number): Promise<MemoryPoint[]> {
    try {
        const res = await apiFetch(`/metrics/ram?since=${sinceSeconds}&step=${stepSeconds}`, {
            method: 'GET',
        });

        if (!res.ok) {
            console.warn(`Failed to fetch RAM usage data`);
            return [];
        }

        const data = await res.json();
        const converted = queryResultToPoints(data);
        return converted || [];
    } catch (err) {
        console.error(`Error while fetching RAM usage data`, err);
        return [];
    }
}

export async function getCPUUsage(sinceSeconds: number, stepSeconds: number): Promise<MemoryPoint[]> {
    try {
        const res = await apiFetch(`/metrics/cpu?since=${sinceSeconds}&step=${stepSeconds}`, {
            method: 'GET',
        });

        if (!res.ok) {
            console.warn(`Failed to fetch RAM usage data`);
            return [];
        }

        const data = await res.json();
        const converted = queryResultToPoints(data);
        return converted || [];
    } catch (err) {
        console.error(`Error while fetching RAM usage data`, err);
        return [];
    }
}

export async function getTempUsage(sinceSeconds: number, stepSeconds: number): Promise<MemoryPoint[]> {
    try {
        const res = await apiFetch(`/metrics/temperature?since=${sinceSeconds}&step=${stepSeconds}`, {
            method: 'GET',
        });

        if (!res.ok) {
            console.warn(`Failed to fetch RAM usage data`);
            return [];
        }

        const data = await res.json();
        const converted = queryResultToPoints(data);
        return converted || [];
    } catch (err) {
        console.error(`Error while fetching RAM usage data`, err);
        return [];
    }
}

export function useRAMUsage(sinceSeconds: number, stepSeconds: number) {
    return useQuery<MemoryPoint[]>({
        queryKey: ['metrics', 'ram', sinceSeconds, stepSeconds],
        queryFn: () => getRAMUsageData(sinceSeconds, stepSeconds),
        refetchOnWindowFocus: true
    });
}

export function useCPUUsage(sinceSeconds: number, stepSeconds: number) {
    return useQuery<MemoryPoint[]>({
        queryKey: ['metrics', 'cpu', sinceSeconds, stepSeconds],
        queryFn: () => getCPUUsage(sinceSeconds, stepSeconds),
        refetchOnWindowFocus: true
    });
}

export function useTempUsage(sinceSeconds: number, stepSeconds: number) {
    return useQuery<MemoryPoint[]>({
        queryKey: ['metrics', 'temperature', sinceSeconds, stepSeconds],
        queryFn: () => getTempUsage(sinceSeconds, stepSeconds),
        refetchOnWindowFocus: true
    });
}

