"use server";
import { revalidatePath } from 'next/cache'

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

const API_CALL_BASE_URL = process.env.NEXT_BACKEND_API_URL;


export async function getServices(): Promise<Service[]> {
  const res = await fetch(`${API_CALL_BASE_URL}/api/services`, { method: 'GET' });
  if (!res.ok) {
    throw new Error(`Failed to fetch services: ${res.status}`);
  }
  return res.json();
}

export async function provisionDNSForServiceName(service_name: string, new_domain: string) {
  const res = await fetch(`${API_CALL_BASE_URL}/api/services/${service_name}/dns/provision`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ "domain": new_domain }),
    cache: 'no-store', // don’t cache the response
  })

  if (!res.ok) {
    const errBody = await res.json().catch(() => ({}))
    throw new Error(
      errBody.error || `Provisioning failed: ${res.status} ${res.statusText}`
    )
  }

  const data = await res.json()

  revalidatePath('/services')

  return data
}

export async function deleteDNSForServiceName(service_name: string) {
  const res = await fetch(`${API_CALL_BASE_URL}/api/services/${service_name}/dns/delete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    cache: 'no-store', // ensure we don’t cache the response
  })

  if (!res.ok) {
    // pull out any error details your Flask API returned
    const errBody = await res.json().catch(() => ({}))
    throw new Error(
      errBody.error || `Deleting DNS domain failed: ${res.status} ${res.statusText}`
    )
  }

  const data = await res.json()

  revalidatePath('/services')

  return data
}
