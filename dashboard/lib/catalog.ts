'use client';

export interface Service {
    key: string;
    service_name: string;
    owner: string;
    description: string;
    created_at: string;
    tags: string[];
    ip?: string;
    port?: number;
    domain?: string;
    notes?: string;
}

const API_CALL_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_API_URL;

export async function getServices(): Promise<Service[]> {
    const token = localStorage.getItem('accessToken');
    try {
        const res = await fetch(`${API_CALL_BASE_URL}/services`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`Failed to fetch services: ${res.status}`);
        }

        const data = await res.json() as Service[];
        return data;
    } catch (err) {
        console.error(`Error while fetching services`, err);
        return [];
    }
}

export async function provisionDNSForServiceName(service_name: string, new_domain: string) {
    const token = localStorage.getItem('accessToken');
    const res = await fetch(`${API_CALL_BASE_URL}/services/dns`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ "domain": new_domain, "service_key": service_name }),
        cache: 'no-store', // don’t cache the response
    })

    if (!res.ok) {
        const errBody = await res.json().catch(() => ({}))
        throw new Error(
            errBody.error || `Provisioning failed: ${res.status} ${res.statusText}`
        )
    }
}

export async function deleteDNSForServiceName(service_name: string) {
    const token = localStorage.getItem('accessToken');
    const res = await fetch(`${API_CALL_BASE_URL}/services/dns/delete`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ "service_key": service_name }),
        cache: 'no-store', // don’t cache the response
    })

    if (!res.ok) {
        const errBody = await res.json().catch(() => ({}))
        throw new Error(
            errBody.error || `Deleting DNS domain failed: ${res.status} ${res.statusText}`
        )
    }
}