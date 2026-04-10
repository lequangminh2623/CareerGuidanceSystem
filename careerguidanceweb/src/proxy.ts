import { NextRequest, NextResponse } from 'next/server';

export default function proxy(request: NextRequest) {
    const token = request.cookies.get('token')?.value;
    const { pathname } = request.nextUrl;

    // Allow access to login and register pages without authentication
    if (
        pathname.startsWith('/login') ||
        pathname.startsWith('/register')
    ) {
        return NextResponse.next();
    }

    // Redirect to login if not authenticated
    if (!(['/login', '/register'].includes(pathname)) && !token) {
        const loginUrl = new URL('/login', request.url);
        return NextResponse.redirect(loginUrl);
    }

    return NextResponse.next();
}

export const config = {
    matcher: [
        /*
          Protect all routes except:
          - /login
          - /register
          - static files (_next, images, etc.)
        */
        '/((?!_next/static|_next/image|favicon.ico|login|register).*)',
    ],
};
