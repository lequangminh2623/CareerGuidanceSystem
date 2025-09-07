'use client';

import { useEffect, useState, useContext, useCallback } from "react";
import { useSearchParams } from "next/navigation";
import { MyUserContext } from "@/lib/contexts/userContext";
import { db } from "@/lib/utils/firebase";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import Sidebar from "@/components/chat/Sidebar";
import ChatPanel from "@/components/chat/ChatPanel";
import { collection, onSnapshot, orderBy, query, where } from "firebase/firestore";
import { useTranslation } from "react-i18next";

interface User {
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    avatar: string;
}

interface Preview {
    text: string;
    timestamp: number;
    sender: string;
}

export default function ChatBox() {
    const user = useContext(MyUserContext) as User | null;
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [previews, setPreviews] = useState<Record<string, Preview>>({});
    const [page, setPage] = useState<number>(1);
    const [loading, setLoading] = useState<boolean>(false);
    const searchParams = useSearchParams();
    const { t } = useTranslation();

    // Fetch users with pagination
    const fetchUsers = useCallback(async () => {
        try {
            setLoading(true);
            let url = `${endpoints['users']}?page=${page}`;
            const kw = searchParams.get('kw');
            if (kw) url += `&kw=${kw}`;

            const res = await Apis.get(url);
            const data: User[] = res.data.content;

            if (data.length === 0) {
                setPage(0);
            } else {
                const filtered = data.filter(u => u.role !== "ROLE_ADMIN");
                setUsers(prev => page === 1 ? filtered : [...prev, ...filtered]);
            }
        } catch (err) {
            console.error("Error fetching users:", err);
        } finally {
            setLoading(false);
        }
    }, [page, searchParams]);

    useEffect(() => {
        if (page > 0) fetchUsers();
    }, [page, fetchUsers]);

    // Reset users when search params change
    useEffect(() => {
        setPage(1);
        setUsers([]);
    }, [searchParams]);

    const loadMore = () => {
        if (!loading && page > 0) setPage(prev => prev + 1);
    };

    // Listen for chat previews
    useEffect(() => {
        if (!user?.email) return;

        const userEmail = user.email;
        const q = query(
            collection(db, "chats"),
            where("participants", "array-contains", userEmail),
            orderBy("updatedAt", "desc")
        );

        const unsub = onSnapshot(q, snapshot => {
            const map: Record<string, Preview> = {};
            snapshot.forEach(doc => {
                const data = doc.data() as any;
                const other = data.participants.find((e: string) => e !== userEmail);
                map[other] = {
                    text: data.lastMessage,
                    timestamp: data.updatedAt?.toMillis() || 0,
                    sender: data.lastSender
                };
            });
            setPreviews(map);
        });

        return () => unsub();
    }, [user]);

    // Handle chatbot interaction
    const handleChatbotInteraction = async (message: string): Promise<string> => {
        try {
            const response = await authApis().post(
                endpoints['ask'],
                { query: message },
                { headers: { "Content-Type": "application/json" } }
            );
            return response?.data ?? t('error-message');
        } catch (err) {
            console.error("Error calling chatbot API:", err);
            return t('error-message');
        }
    };

    if (!user) return <div>{t('loading-user')}</div>;

    return (
        <div className="flex h-[85vh]">
            <Sidebar
                users={users}
                currentEmail={user.email}
                selectedUser={selectedUser}
                onSelect={setSelectedUser}
                previews={previews}
                onEndReach={loadMore}
            />

            {selectedUser ? (
                <ChatPanel
                    selectedUser={selectedUser}
                    onChatbotInteraction={
                        selectedUser.email === "chatbot@ou.edu.vn"
                            ? handleChatbotInteraction
                            : undefined
                    }
                />
            ) : (
                <div className="flex-1 flex items-center justify-center text-gray-500">
                    {t('select-chat')}
                </div>
            )}
        </div>
    );
}
