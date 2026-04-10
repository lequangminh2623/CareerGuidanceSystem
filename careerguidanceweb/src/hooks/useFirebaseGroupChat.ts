"use client";

import { useState, useEffect, useCallback, useRef } from "react";
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
    deleteDoc,
    Timestamp,
    limit,
    arrayUnion,
    arrayRemove,
} from "firebase/firestore";
import { User } from "firebase/auth";
import { db } from "@/lib/utils/firebase";

/** Local UID sanitization to match index.tsx logic */
function sanitizeUid(uid: string): string {
    return uid.replace(/@/g, "_at_").replace(/\+/g, "_plus_");
}

// ===================== Types =====================

export interface GroupRoom {
    id: string;
    groupName: string;
    adminId: string;
    members: string[];
    lastMessage?: string | null;
    lastMessageAt?: Timestamp | null;
    lastSenderId?: string | null;
    seenBy: string[];
    createdAt?: Timestamp | null;
    type: "group";
}

export interface GroupMessage {
    id: string;
    groupId: string;
    senderId: string;
    senderName?: string;
    text?: string;
    createdAt: Timestamp | null;
}

// ===================== Hook =====================

export function useFirebaseGroupChat(firebaseUser: User | null) {
    const [groupRooms, setGroupRooms] = useState<GroupRoom[]>([]);
    const [groupMessages, setGroupMessages] = useState<GroupMessage[]>([]);
    const [activeGroupId, setActiveGroupId] = useState<string | null>(null);

    const messagesUnsubRef = useRef<(() => void) | null>(null);
    const groupRoomsRef = useRef<GroupRoom[]>([]);
    const sanitizedUid = firebaseUser ? sanitizeUid(firebaseUser.uid) : "";

    // ---- 1. Listen to group rooms the current user is a member of ----
    useEffect(() => {
        if (!firebaseUser || !sanitizedUid) return;

        const groupsRef = collection(db, "groups");
        const q = query(
            groupsRef,
            where("members", "array-contains", sanitizedUid)
        );

        const unsubscribe = onSnapshot(
            q,
            (snapshot) => {
                const rooms: GroupRoom[] = snapshot.docs.map((doc) => ({
                    id: doc.id,
                    ...(doc.data() as Omit<GroupRoom, "id">),
                }));
                // Sort by lastMessageAt descending
                rooms.sort((a, b) => {
                    const aTime = a.lastMessageAt?.toMillis() ?? 0;
                    const bTime = b.lastMessageAt?.toMillis() ?? 0;
                    return bTime - aTime;
                });
                setGroupRooms(rooms);
                groupRoomsRef.current = rooms;
            },
            (err) => {
                console.error("Error listening to group rooms:", err);
            }
        );

        return () => unsubscribe();
    }, [firebaseUser, sanitizedUid]);

    // ---- 2. Open a group chat (listen to messages) ----
    const openGroup = useCallback(
        (groupId: string) => {
            // Unsubscribe from previous group messages
            if (messagesUnsubRef.current) {
                messagesUnsubRef.current();
            }

            setActiveGroupId(groupId);
            setGroupMessages([]);

            const messagesRef = collection(db, "groups", groupId, "messages");
            const q = query(messagesRef, orderBy("createdAt", "asc"), limit(200));

            const unsubscribe = onSnapshot(
                q,
                (snapshot) => {
                    const msgs: GroupMessage[] = snapshot.docs.map((d) => ({
                        id: d.id,
                        groupId,
                        ...(d.data() as Omit<GroupMessage, "id" | "groupId">),
                    }));
                    setGroupMessages(msgs);
                },
                (err) => {
                    console.error("Error listening to group messages:", err);
                }
            );

            messagesUnsubRef.current = unsubscribe;
        },
        []
    );

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            if (messagesUnsubRef.current) {
                messagesUnsubRef.current();
            }
        };
    }, []);

    // ---- 3. Send a group message ----
    const sendGroupMessage = useCallback(
        async (groupId: string, text: string, senderName?: string) => {
            if (!firebaseUser || !text.trim() || !sanitizedUid) return;

            const messagesRef = collection(db, "groups", groupId, "messages");
            await addDoc(messagesRef, {
                senderId: sanitizedUid,
                senderName: senderName || sanitizedUid,
                text: text.trim(),
                createdAt: serverTimestamp(),
            });

            // Update lastMessage on the group document and reset seenBy to only the sender
            const groupRef = doc(db, "groups", groupId);
            await updateDoc(groupRef, {
                lastMessage: text.trim(),
                lastMessageAt: serverTimestamp(),
                lastSenderId: sanitizedUid,
                seenBy: [sanitizedUid],
            });
        },
        [firebaseUser, sanitizedUid]
    );

    // ---- 4. Mark group as seen ----
    const markGroupAsSeen = useCallback(
        async (groupId: string) => {
            if (!firebaseUser || !sanitizedUid || !groupId) return;
            try {
                const groupRef = doc(db, "groups", groupId);
                await updateDoc(groupRef, {
                    seenBy: arrayUnion(sanitizedUid),
                });
            } catch (err: unknown) {
                // Ignore if the document was already deleted during a race condition
                const firebaseError = err as { code?: string };
                if (firebaseError?.code === 'not-found') return;
                console.error("Failed to mark group as seen:", err);
            }
        },
        [firebaseUser, sanitizedUid]
    );

    // ---- 5. Create a group ----
    const createGroup = useCallback(
        async (groupName: string, memberUids: string[]): Promise<string> => {
            if (!firebaseUser || !sanitizedUid)
                throw new Error("Not authenticated with Firebase");

            // Include admin in members
            const allMembers = Array.from(
                new Set([sanitizedUid, ...memberUids])
            );

            const groupsRef = collection(db, "groups");
            const docRef = await addDoc(groupsRef, {
                groupName,
                adminId: sanitizedUid,
                members: allMembers,
                lastMessage: null,
                lastMessageAt: null,
                lastSenderId: null,
                seenBy: [],
                createdAt: serverTimestamp(),
                type: "group",
            });

            return docRef.id;
        },
        [firebaseUser, sanitizedUid]
    );

    // ---- 6. Add member (admin only) ----
    const addMember = useCallback(
        async (groupId: string, newUserUid: string) => {
            if (!firebaseUser || !sanitizedUid)
                throw new Error("Not authenticated with Firebase");

            // Verify admin using ref for latest data
            const group = groupRoomsRef.current.find((g) => g.id === groupId);
            if (!group || group.adminId !== sanitizedUid) {
                throw new Error("Only the admin can add members");
            }

            const groupRef = doc(db, "groups", groupId);
            await updateDoc(groupRef, {
                members: arrayUnion(newUserUid),
            });
        },
        [firebaseUser, sanitizedUid]
    );

    // ---- 7. Remove member (admin only) ----
    const removeMember = useCallback(
        async (groupId: string, userUid: string) => {
            if (!firebaseUser || !sanitizedUid)
                throw new Error("Not authenticated with Firebase");

            // Verify admin using ref for latest data
            const group = groupRoomsRef.current.find((g) => g.id === groupId);
            if (!group || group.adminId !== sanitizedUid) {
                throw new Error("Only the admin can remove members");
            }

            const groupRef = doc(db, "groups", groupId);
            await updateDoc(groupRef, {
                members: arrayRemove(userUid),
            });
        },
        [firebaseUser, sanitizedUid]
    );

    // ---- 8. Delete group (admin only) ----
    const deleteGroup = useCallback(
        async (groupId: string) => {
            if (!firebaseUser || !sanitizedUid)
                throw new Error("Not authenticated with Firebase");

            // Verify admin using ref for latest data
            const group = groupRoomsRef.current.find((g) => g.id === groupId);
            if (!group || group.adminId !== sanitizedUid) {
                console.error("[deleteGroup] Admin check failed", { groupAdminId: group?.adminId, currentUid: sanitizedUid });
                throw new Error("Only the admin can delete the group");
            }

            // Clear state BEFORE deleting to prevent other hooks/components from trying to update it
            if (activeGroupId === groupId) {
                setActiveGroupId(null);
                setGroupMessages([]);
            }

            const groupRef = doc(db, "groups", groupId);
            await deleteDoc(groupRef);
            console.log("[deleteGroup] Document deleted:", groupId);
        },
        [firebaseUser, activeGroupId, sanitizedUid]
    );

    // ---- 9. Close group (clear active) ----
    const closeGroup = useCallback(() => {
        if (messagesUnsubRef.current) {
            messagesUnsubRef.current();
            messagesUnsubRef.current = null;
        }
        setActiveGroupId(null);
        setGroupMessages([]);
    }, []);

    return {
        // State
        groupRooms,
        groupMessages,
        activeGroupId,

        // Actions
        openGroup,
        closeGroup,
        sendGroupMessage,
        markGroupAsSeen,
        createGroup,
        addMember,
        removeMember,
        deleteGroup,
    };
}
