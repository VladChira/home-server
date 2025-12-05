import { NextResponse } from 'next/server';
import { NextRequest } from 'next/server';

const API_BASE = process.env.NEXT_PUBLIC_BACKEND_API_URL;

const protectedRoutes = ["/", "/virtual-machines/", "/minecraft/", "/services/"]
const publicRoutes = ["/auth/"]

export default async function middleware(req: NextRequest) {
    const path = req.nextUrl.pathname;
    const isProtected = protectedRoutes.includes(path);

    if (isProtected) {
        const token = req.cookies.get('accessToken')?.value;


        if (!token) {
            console.log('here');
            const url = req.nextUrl.clone();
            url.pathname = '/auth';
            return NextResponse.redirect(url);
        }

        // Call backend /me to validate token
        try {
            const res = await fetch(`${API_BASE}/login/me`, {
                method: 'GET',
                headers: { Authorization: `Bearer ${token}` },
            });

            if (!res.ok) {
                const url = req.nextUrl.clone();
                url.pathname = '/auth';
                return NextResponse.redirect(url);
            }
        } catch {
        }
    }


}
