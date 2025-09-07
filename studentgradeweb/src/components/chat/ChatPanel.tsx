'use client';

import { useEffect, useState, useContext, useRef, FormEvent } from "react";
import ReactMarkdown from "react-markdown";
import { db } from "@/lib/utils/firebase";
import { MyUserContext } from "@/lib/contexts/userContext";
import { collection, addDoc, serverTimestamp, doc, setDoc, onSnapshot } from "firebase/firestore";
import { useTranslation } from "react-i18next";

interface SelectedUser {
    email: string;
    lastName: string;
    firstName: string;
}

interface ChatPanelProps {
    selectedUser: SelectedUser;
    onChatbotInteraction?: (message: string) => Promise<string>;
}

interface Message {
    id: string;
    text: string;
    sender: string;
    timestamp: { seconds: number; nanoseconds: number } | null;
}

export default function ChatPanel({ selectedUser, onChatbotInteraction }: ChatPanelProps) {
    const user = useContext(MyUserContext);
    const [text, setText] = useState<string>("");
    const [messages, setMessages] = useState<Message[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const { t } = useTranslation();
    const chatHistoryRef = useRef<HTMLDivElement>(null);
    const endRef = useRef<HTMLDivElement>(null);

    if (!user) return null;

    const current = user.email;
    const other = selectedUser.email;
    const chatId = [current, other].sort().join("_");
    const chatsRef = doc(db, "chats", chatId);
    const messagesColl = collection(db, "chats", chatId, "messages");

    // Listen for realtime messages
    useEffect(() => {
        const unsubscribe = onSnapshot(messagesColl, snapshot => {
            const msgs: Message[] = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data(),
            })) as Message[];

            msgs.sort((a, b) => (a.timestamp?.seconds || 0) - (b.timestamp?.seconds || 0));
            setMessages(msgs);
        });

        return () => unsubscribe();
    }, [selectedUser.email]);

    // Scroll to bottom when messages update
    useEffect(() => {
        endRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, loading]);

    const sendMessage = async (e: FormEvent) => {
        e.preventDefault();
        if (!text.trim()) return;

        const userMessage = {
            text,
            sender: current,
            timestamp: serverTimestamp(),
        };

        try {
            setText("");
            setLoading(true);

            // Save user's message
            await addDoc(messagesColl, userMessage);
            await setDoc(
                chatsRef,
                {
                    participants: [current, other],
                    lastMessage: text,
                    lastSender: current,
                    updatedAt: serverTimestamp(),
                },
                { merge: true }
            );

            // Chatbot response
            if (onChatbotInteraction) {
                const response = await onChatbotInteraction(text);
                const botMessage = {
                    text: response,
                    sender: "chatbot@ou.edu.vn",
                    timestamp: serverTimestamp(),
                };

                await addDoc(messagesColl, botMessage);
                await setDoc(
                    chatsRef,
                    {
                        participants: [current, other],
                        lastMessage: response,
                        lastSender: "chatbot@ou.edu.vn",
                        updatedAt: serverTimestamp(),
                    },
                    { merge: true }
                );
            }
        } catch (err) {
            console.error("Error sending message:", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col flex-1 bg-white h-full">
            <div className="p-4 border-b">
                <h5 className="font-medium">{`${selectedUser.lastName} ${selectedUser.firstName}`}</h5>
            </div>

            <div ref={chatHistoryRef} className="flex-1 overflow-y-auto p-4 space-y-4">
                {messages.map((m) => (
                    <div
                        key={m.id}
                        className={`flex ${m.sender === current ? "justify-end" : "justify-start"}`}
                    >
                        <div
                            className={`max-w-[70%] rounded-lg p-3 ${m.sender === current ? "bg-primary text-white" : "bg-gray-100"
                                }`}
                        >
                            <ReactMarkdown>{m.text}</ReactMarkdown>
                        </div>
                    </div>
                ))}

                {loading && (
                    <div className="flex justify-start">
                        <div className="bg-gray-100 rounded-lg p-3">...</div>
                    </div>
                )}

                <div ref={endRef} />
            </div>

            {/* Input */}
            <form onSubmit={sendMessage} className="p-4 border-t flex gap-2">
                <input
                    type="text"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder={t("enter")}
                    disabled={loading}
                    className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
                <button
                    type="submit"
                    disabled={loading}
                    className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark disabled:opacity-50"
                >
                    {t("send")}
                </button>
            </form>
        </div>
    );
}
