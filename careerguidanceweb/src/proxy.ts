import { NextRequest, NextResponse } from 'next/server';

/**
 * Decode JWT payload và kiểm tra thời hạn (exp).
 * Không cần verify signature vì chỉ dùng để check expiry ở middleware.
 */
function isTokenExpired(token: string): boolean {
    try {
        const payloadBase64 = token.split('.')[1];
        if (!payloadBase64) return true;

        // Base64url → Base64 → decode
        const base64 = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = Buffer.from(base64, 'base64').toString('utf-8');
        const payload = JSON.parse(jsonPayload) as { exp?: number };

        if (!payload.exp) return false; // Không có exp → coi như không hết hạn
        return Date.now() >= payload.exp * 1000;
    } catch {
        return true; // Parse lỗi → coi như hết hạn
    }
}

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

    // Redirect to login if token is missing or expired
    if (!token || isTokenExpired(token)) {
        const loginUrl = new URL('/login', request.url);
        const response = NextResponse.redirect(loginUrl);
        // Xóa cookie token hết hạn để tránh vòng lặp
        if (token) {
            response.cookies.delete('token');
        }
        return response;
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
