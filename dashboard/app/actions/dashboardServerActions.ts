'use server';

const promURL = process.env.PROMETHEUS_URL;

type MemoryPoint = {
    time: string; // formatted timestamp
    value: number; // percentage used
};

function urlfromQuery(query: string, since: number, step: number): string {
    const now = Math.floor(Date.now() / 1000);
    const start = now - since
    const url = `${promURL}/api/v1/query_range?query=${encodeURIComponent(query)}&start=${start}&end=${now}&step=${step}`;
    return url;
}

function queryResultToPoints(series: { values: [number, string][]; }): MemoryPoint[] {
    const result: MemoryPoint[] = series.values.map(([ts, val]: [number, string]) => {
        const date = new Date(ts * 1000);
        const label = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
        return {
            time: label,
            value: parseFloat(val),
        };
    });
    return result;
}

export async function fetchMemoryUsage(): Promise<MemoryPoint[]> {
    const query = `(1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100`;

    const url = urlfromQuery(query, 60 * 60 * 24, 60 * 30); // since 24h ago, every 30min

    const res = await fetch(url, {
        headers: {
            Accept: 'application/json',
        },
        cache: 'no-store',
    });

    if (!res.ok) {
        throw new Error(`Failed to fetch Prometheus data: ${res.statusText}`);
    }

    const json = await res.json();
    const series = json.data.result[0];

    if (!series || !series.values) return [];

    return queryResultToPoints(series);
}

export async function fetchCPUUsage(): Promise<MemoryPoint[]> {
    const query = `(1-rate(node_cpu_seconds_total{mode="idle"}[1m]))*100`;

    const url = urlfromQuery(query, 60 * 60 * 24, 60 * 30); // since 24h ago, every 30min

    const res = await fetch(url, {
        headers: {
            Accept: 'application/json',
        },
        cache: 'no-store',
    });

    if (!res.ok) {
        throw new Error(`Failed to fetch Prometheus data: ${res.statusText}`);
    }

    const json = await res.json();
    const series = json.data.result[0];

    if (!series || !series.values) return [];

    return queryResultToPoints(series);
}

export async function fetchTemperature(): Promise<MemoryPoint[]> {
    const query = `avg without(sensor) (node_hwmon_temp_celsius * on(chip) group_left(chip_name) node_hwmon_chip_names)`;

    const url = urlfromQuery(query, 60 * 60 * 24, 60 * 30); // since 24h ago, every 30min

    const res = await fetch(url, {
        headers: {
            Accept: 'application/json',
        },
        cache: 'no-store',
    });

    if (!res.ok) {
        throw new Error(`Failed to fetch Prometheus data: ${res.statusText}`);
    }

    const json = await res.json();
    const series = json.data.result[1]; // the second one is "coretemp"

    if (!series || !series.values) return [];

    return queryResultToPoints(series);
}

