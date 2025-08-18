'use client';

import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "./api";

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

export async function getServices(): Promise<Service[]> {
    try {
        const res = await apiFetch(`/services`, { method: 'GET' });

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
    const body = JSON.stringify({ "domain": new_domain, "service_key": service_name })
    const res = await apiFetch("/services/dns", { method: 'POST', body: body })

    if (!res.ok) {
        const errBody = await res.json().catch(() => ({}))
        throw new Error(
            errBody.error || `Provisioning failed: ${res.status} ${res.statusText}`
        )
    }
}

export async function deleteDNSForServiceName(service_name: string) {
    const body = JSON.stringify({ "service_key": service_name })
    const res = await apiFetch("/services/dns/delete", { method: 'POST', body: body })

    if (!res.ok) {
        const errBody = await res.json().catch(() => ({}))
        throw new Error(
            errBody.error || `Deleting DNS domain failed: ${res.status} ${res.statusText}`
        )
    }
}

export async function onboardNewService(service: Service) {
    const body = JSON.stringify(service)
    const res = await apiFetch("/services", { method: 'POST', body: body })

    if (!res.ok) {
        const errBody = await res.json().catch(() => ({}))
        throw new Error(
            errBody.error || `Onboarding new service ${res.status} ${res.statusText}`
        )
    }
}

export function useCatalogServices() {
    return useQuery<Service[]>({
        queryKey: ['services'],
        queryFn: getServices,
        refetchOnWindowFocus: true
    });
}