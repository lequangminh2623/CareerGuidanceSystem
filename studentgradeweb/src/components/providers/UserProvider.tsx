'use client';

import { useReducer, useEffect } from "react";
import { getCookie } from "cookies-next";
import { MyUserContext, MyDispatcherContext } from "@/lib/contexts/userContext";
import myUserReducer from "@/lib/reducers/userReducer";
import { authApis, endpoints } from "@/lib/utils/api";

export default function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, dispatch] = useReducer(myUserReducer, null);

    useEffect(() => {
        const restoreUser = async () => {
            const token = getCookie('token');
            if (token && !user) {
                try {
                    const response = await authApis().get(endpoints['profile']);
                    dispatch({ type: 'login', payload: response.data });
                } catch (error) {
                    console.error('Failed to restore user:', error);
                    // Token might be expired, remove it
                    dispatch({ type: 'logout' });
                }
            }
        };

        restoreUser();
    }, []); // Remove user dependency to prevent infinite loop

    return (
        <MyUserContext.Provider value={user}>
            <MyDispatcherContext.Provider value={dispatch}>
                {children}
            </MyDispatcherContext.Provider>
        </MyUserContext.Provider>
    );
}
