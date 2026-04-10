"use client";

import { GoogleOAuthProvider } from "@react-oauth/google";
import { useRef, useCallback, useEffect } from "react";

export default function GoogleProvider({ children }: { children: React.ReactNode }) {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    const initializedRef = useRef(false);

    useEffect(() => {
        if (initializedRef.current) return;
        initializedRef.current = true;

        const checkAndPatch = () => {
            const gsi = (window as any).google?.accounts?.id;
            if (!gsi) return;

            const originalInit = gsi.initialize;
            let callCount = 0;

            gsi.initialize = function (...args: any[]) {
                callCount++;
                if (callCount <= 1) {
                    return originalInit.apply(this, args);
                }
                // Silently skip subsequent calls (caused by Strict Mode remount)
            };
        };

        // The GSI script loads async, so we need to wait for it
        const interval = setInterval(() => {
            if ((window as any).google?.accounts?.id) {
                checkAndPatch();
                clearInterval(interval);
            }
        }, 50);

        // Cleanup: stop checking after 5 seconds
        const timeout = setTimeout(() => clearInterval(interval), 5000);

        return () => {
            clearInterval(interval);
            clearTimeout(timeout);
        };
    }, []);

    if (!clientId) return <>{children}</>;

    return (
        <GoogleOAuthProvider clientId={clientId}>
            {children}
        </GoogleOAuthProvider>
    );
}
