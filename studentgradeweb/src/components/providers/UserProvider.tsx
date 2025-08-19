'use client';

import { useReducer } from "react";
import { MyUserContext, MyDispatcherContext } from "@/lib/contexts/userContext";
import myUserReducer from "@/lib/reducers/userReducer";

export default function UserProvider({ children }: { children: React.ReactNode }) {
    const [user, dispatch] = useReducer(myUserReducer, null);

    return (
        <MyUserContext.Provider value={user}>
            <MyDispatcherContext.Provider value={dispatch}>
                {children}
            </MyDispatcherContext.Provider>
        </MyUserContext.Provider>
    );
}
