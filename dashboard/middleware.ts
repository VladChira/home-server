import { decrypt } from "@/lib/session";
import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

const protectedRoutes = ["/", "/virtual-machines/", "/minecraft/", "/services/"]
const publicRoutes = ["/auth/"]

export default async function middleware(req: NextRequest) {
    const path = req.nextUrl.pathname;
    const isProtected = protectedRoutes.includes(path);
    const isPublic = publicRoutes.includes(path);

    console.log(path)

    const cookie = (await cookies()).get('session')?.value;

    const session = await decrypt(cookie);

    if (isProtected && !session?.userId) {
        const url = req.nextUrl.clone();
        url.pathname = '/auth';
        return NextResponse.redirect(url);
    }

    if (isPublic && session?.userId) {
        const url = req.nextUrl.clone();
        url.pathname = '/';
        return NextResponse.redirect(url);
    }
}
