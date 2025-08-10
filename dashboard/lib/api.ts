
const API_CALL_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_API_URL;

export async function apiFetch(path: string, init: RequestInit = {}) {
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;

    const headers = new Headers(init.headers || {});
    headers.set('Accept', 'application/json');
    headers.set('Content-Type', 'application/json');

    if (token) headers.set('Authorization', `Bearer ${token}`);

    const base = API_CALL_BASE_URL;
    const res = await fetch(`${base}${path}`, { ...init, headers });

    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    return res;
}
