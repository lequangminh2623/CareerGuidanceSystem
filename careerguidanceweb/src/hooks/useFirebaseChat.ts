"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import { signInWithCustomToken, onAuthStateChanged, User } from "firebase/auth";
import {
    collection,
    query,
    where,
    orderBy,
    onSnapshot,
    addDoc,
    serverTimestamp,
    doc,
    updateDoc,
    Timestamp,
    limit,
    arrayUnion,
} from "firebase/firestore";
import { auth, db } from "@/lib/utils/firebase";
import { authApis } from "@/lib/utils/api";

// ===================== Types =====================

export interface ChatMessage {
    id: string;
    chatId: string;
    senderId: string;
    senderName?: string;
    text?: string;
    createdAt: Timestamp | null;
}

export interface ChatRoom {
    id: string;
    type: "direct" | "group";
    name?: string;
    sectionId?: string;
    members: string[];
    lastMessage?: string | null;
    lastMessageAt?: Timestamp | null;
    lastSenderId?: string | null;
    seenBy?: string[];
}

// ===================== Hook =====================

export function useFirebaseChat() {
    const [firebaseUser, setFirebaseUser] = useState<User | null>(null);
    const [chatRooms, setChatRooms] = useState<ChatRoom[]>([]);
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [activeChatId, setActiveChatId] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const messagesUnsubRef = useRef<(() => void) | null>(null);

    // ---- 1. Authenticate with Firebase using Custom Token ----
    const authenticateWithFirebase = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            // Fetch custom token from chat-service via API Gateway
            const res = await authApis().get(
                "/chat-service/api/secure/chat/token"
            );
            const { customToken } = res.data;

            // Sign in to Firebase with the custom token
            const credential = await signInWithCustomToken(auth, customToken);
            setFirebaseUser(credential.user);
            return credential.user;
        } catch (err: unknown) {
            const message =
                err instanceof Error ? err.message : "Firebase auth failed";
            setError(message);
            console.error("Firebase authentication error:", err);
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    // ---- 2. Listen for auth state changes ----
    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, (user) => {
            setFirebaseUser(user);
            if (!user) {
                setChatRooms([]);
                setMessages([]);
            }
        });
        return () => unsubscribe();
    }, []);

    // ---- 3. Listen to chat rooms the current user is a member of ----
    useEffect(() => {
        if (!firebaseUser) return;

        const chatsRef = collection(db, "chats");
        const q = query(
            chatsRef,
            where("members", "array-contains", firebaseUser.uid)
        );

        const unsubscribe = onSnapshot(
            q,
            (snapshot) => {
                const rooms: ChatRoom[] = snapshot.docs.map((doc) => ({
                    id: doc.id,
                    ...(doc.data() as Omit<ChatRoom, "id">),
                }));
                // Sort by lastMessageAt descending
                rooms.sort((a, b) => {
                    const aTime = a.lastMessageAt?.toMillis() ?? 0;
                    const bTime = b.lastMessageAt?.toMillis() ?? 0;
                    return bTime - aTime;
                });
                setChatRooms(rooms);
            },
            (err) => {
                console.error("Error listening to chat rooms:", err);
                setError("Failed to load chat rooms");
            }
        );

        return () => unsubscribe();
    }, [firebaseUser]);

    // ---- 4. Listen to messages of the active chat ----
    const openChat = useCallback((chatId: string) => {
        // Unsubscribe from previous chat
        if (messagesUnsubRef.current) {
            messagesUnsubRef.current();
        }

        setActiveChatId(chatId);
        setMessages([]);

        if (chatId.startsWith("TEMP_")) {
            return; // Lazy creation - room doesn't exist yet
        }

        const messagesRef = collection(db, "chats", chatId, "messages");
        const q = query(messagesRef, orderBy("createdAt", "asc"), limit(200));

        const unsubscribe = onSnapshot(
            q,
            (snapshot) => {
                const msgs: ChatMessage[] = snapshot.docs.map((d) => ({
                    id: d.id,
                    chatId,
                    ...(d.data() as Omit<ChatMessage, "id" | "chatId">),
                }));
                setMessages(msgs);
            },
            (err) => {
                console.error("Error listening to messages:", err);
            }
        );

        messagesUnsubRef.current = unsubscribe;
    }, []);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            if (messagesUnsubRef.current) {
                messagesUnsubRef.current();
            }
        };
    }, []);

    // ---- 5. Send a text message ----
    const sendMessage = useCallback(
        async (chatId: string, text: string, senderName?: string) => {
            if (!firebaseUser || !text.trim()) return;

            const messagesRef = collection(db, "chats", chatId, "messages");
            await addDoc(messagesRef, {
                senderId: firebaseUser.uid,
                senderName: senderName || firebaseUser.uid,
                text: text.trim(),
                createdAt: serverTimestamp(),
            });

            // Update lastMessage on the chat document and reset seenBy to only the sender
            const chatRef = doc(db, "chats", chatId);
            await updateDoc(chatRef, {
                lastMessage: text.trim(),
                lastMessageAt: serverTimestamp(),
                seenBy: [firebaseUser.uid],
            });
        },
        [firebaseUser]
    );

    // ---- 7. Create a 1-on-1 chat ----
    const createDirectChat = useCallback(
        async (otherUserUid: string): Promise<string> => {
            if (!firebaseUser)
                throw new Error("Not authenticated with Firebase");

            const members = [firebaseUser.uid, otherUserUid].sort();
            const chatId = members.join("__");

            // Use set with merge to avoid duplicating
            const { setDoc } = await import("firebase/firestore");
            const chatRef = doc(db, "chats", chatId);
            await setDoc(
                chatRef,
                {
                    type: "direct",
                    members,
                    createdAt: serverTimestamp(),
                    lastMessage: null,
                    lastMessageAt: null,
                },
                { merge: true }
            );

            return chatId;
        },
        [firebaseUser]
    );

    // ---- 8. Mark chat as seen ----
    const markAsSeen = useCallback(async (chatId: string) => {
        if (!firebaseUser || chatId.startsWith("TEMP_")) return;
        try {
            const chatRef = doc(db, "chats", chatId);
            await updateDoc(chatRef, {
                seenBy: arrayUnion(firebaseUser.uid)
            });
        } catch (err) {
            console.error("Failed to mark as seen:", err);
        }
    }, [firebaseUser]);

    return {
        // State
        firebaseUser,
        chatRooms,
        messages,
        activeChatId,
        loading,
        error,

        // Actions
        authenticateWithFirebase,
        openChat,
        sendMessage,
        createDirectChat,
        markAsSeen,
    };
}
