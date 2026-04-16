'use client';

import { useEffect } from "react";
import { getCookie, deleteCookie } from "cookies-next";
import { useRouter } from "next/navigation";
import { useAppDispatch } from "@/store/hooks";
import { loginSuccess, logout } from "@/store/features/auth/authSlice";
import { authApis, endpoints } from "@/lib/utils/api";

export default function UserProvider({ children }: { children: React.ReactNode }) {
    const dispatch = useAppDispatch();
    const router = useRouter();

    useEffect(() => {
        const restoreUser = async () => {
            const token = getCookie('token');
            if (token) {
                try {
                    const response = await authApis().get(endpoints['profile']);
                    dispatch(loginSuccess(response.data));
                } catch (error: unknown) {
                    console.error('Failed to restore user session:', error);
                    deleteCookie('token');
                    dispatch(logout());
                    router.replace('/login');
                }
            }
        };

        restoreUser();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []); // Run once on mount

    return <>{children}</>;
}
