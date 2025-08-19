'use client';

import { Dispatch, SetStateAction, useRef } from "react";
import Image from "next/image";
import TimeConvert from "@/components/layout/TimeConvert";

interface User {
    email: string;
    lastName: string;
    firstName: string;
    avatar: string;
    role: string;
}

interface Preview {
    text?: string;
    sender?: string;
    timestamp?: number;
}

interface SidebarProps {
    users: User[];
    currentEmail: string;
    selectedUser: User | null;
    onSelect: Dispatch<SetStateAction<User | null>>;
    previews: Record<string, Preview>;
    onEndReach: () => void;
}

export default function Sidebar({
    users,
    currentEmail,
    selectedUser,
    onSelect,
    previews = {},
    onEndReach
}: SidebarProps) {

    const loadingRef = useRef(false);

    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
        if (
            !loadingRef.current &&
            scrollHeight > clientHeight &&
            scrollTop + clientHeight >= scrollHeight - 10
        ) {
            loadingRef.current = true;
            Promise.resolve(onEndReach())
                .finally(() => {
                    loadingRef.current = false;
                });
        }
    };

    return (
        <div
            className="w-80 border-r bg-white overflow-y-auto"
            onScroll={handleScroll}
        >
            <h3 className="p-4 font-bold text-lg border-b">Chats</h3>
            <ul className="divide-y">
                {Array.isArray(users) &&
                    users.map(u => {
                        const fullName = `${u.lastName} ${u.firstName}`;
                        const p = previews[u.email] || {};
                        const previewText = p.text
                            ? (p.sender === currentEmail ? `You: ${p.text}` : p.text)
                            : "No messages yet";

                        return (
                            <li
                                key={u.email}
                                className={`p-4 cursor-pointer hover:bg-gray-50 ${selectedUser?.email === u.email ? 'bg-gray-100' : ''
                                    }`}
                                onClick={() => onSelect(u)}
                            >
                                <div className="flex items-center space-x-3">
                                    <Image
                                        src={u.avatar}
                                        alt={fullName}
                                        width={40}
                                        height={40}
                                        className="rounded-full"
                                    />
                                    <div className="flex-1 min-w-0">
                                        <p className="font-medium truncate">{fullName}</p>
                                        <p className="text-sm text-gray-500 truncate">{previewText}</p>
                                    </div>
                                    {p.timestamp && (
                                        <div className="text-xs text-gray-400">
                                            <TimeConvert timestamp={p.timestamp} />
                                        </div>
                                    )}
                                </div>
                            </li>
                        );
                    })}
            </ul>
        </div>
    );
}
