'use client';

import { useEffect, useState, useRef, FormEvent, useMemo } from "react";
import Image from "next/image";
import { ChatMessage, useFirebaseChat } from "@/hooks/useFirebaseChat";
import { GroupMessage, useFirebaseGroupChat } from "@/hooks/useFirebaseGroupChat";
import { PaperAirplaneIcon, Cog6ToothIcon } from "@heroicons/react/24/solid";
import { ApiUser } from "@/components/chat/index";
import { useTranslation } from "react-i18next";

interface ChatPanelProps {
    currentUid: string;
    activeChatId: string;
    otherUser: ApiUser;
    firebaseChat: ReturnType<typeof useFirebaseChat>;
    // Group mode props
    isGroup?: boolean;
    groupName?: string;
    groupAvatar?: string;
    groupMessages?: GroupMessage[];
    groupChatHook?: ReturnType<typeof useFirebaseGroupChat>;
    onOpenGroupSettings?: () => void;
    currentUserName?: string;
    allUsers?: ApiUser[];
}

export default function ChatPanel({
    currentUid,
    activeChatId,
    otherUser,
    firebaseChat,
    isGroup = false,
    groupName,
    groupAvatar,
    groupMessages,
    groupChatHook,
    onOpenGroupSettings,
    currentUserName,
    allUsers = [],
}: ChatPanelProps) {
    const { t } = useTranslation();
    const [text, setText] = useState("");
    const endRef = useRef<HTMLDivElement>(null);

    const { messages: directMessages, sendMessage, markAsSeen, createDirectChat, openChat } = firebaseChat;

    // Use group messages if in group mode, otherwise use direct messages
    const messages: (ChatMessage | GroupMessage)[] = useMemo(() =>
        isGroup ? (groupMessages ?? []) : directMessages,
        [isGroup, groupMessages, directMessages]);

    // Scroll to bottom
    useEffect(() => {
        endRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages]);

    // Mark as seen when chat is opened or new messages arrive
    useEffect(() => {
        if (isGroup && groupChatHook && activeChatId) {
            groupChatHook.markGroupAsSeen(activeChatId);
        } else if (activeChatId) {
            markAsSeen(activeChatId);
        }
    }, [activeChatId, messages.length, markAsSeen, isGroup, groupChatHook]);

    const handleSendMessage = async (e: FormEvent) => {
        e.preventDefault();
        const trimmed = text.trim();
        if (!trimmed) return;

        setText("");

        if (isGroup && groupChatHook) {
            // Group message
            await groupChatHook.sendGroupMessage(activeChatId, trimmed, currentUserName);
            groupChatHook.markGroupAsSeen(activeChatId);
        } else {
            let targetChatId = activeChatId;

            // If this is a lazy-created chat, create the document first
            if (targetChatId.startsWith("TEMP_")) {
                const otherUserUid = targetChatId.replace("TEMP_", "");
                try {
                    targetChatId = await createDirectChat(otherUserUid);
                    openChat(targetChatId);
                } catch (error) {
                    console.error("Failed to create chat room:", error);
                    return;
                }
            }

            await sendMessage(targetChatId, trimmed, currentUserName);
            markAsSeen(targetChatId);
        }
    };

    const formatMessageTime = (message: ChatMessage | GroupMessage) => {
        if (!message.createdAt) return "";
        const date = message.createdAt.toDate ? message.createdAt.toDate() : new Date((message.createdAt as { seconds: number }).seconds * 1000);
        return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
    };

    const displayName = isGroup ? groupName : `${otherUser.lastName} ${otherUser.firstName}`;
    const displayAvatar = isGroup
        ? (groupAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(groupName || 'Group')}&background=random&color=fff&bold=true`)
        : (otherUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(otherUser.lastName + ' ' + otherUser.firstName)}&background=6366f1&color=fff&bold=true`);

    return (
        <div className="flex flex-col flex-1 h-full bg-white/40 backdrop-blur-md text-gray-800 border-l border-white/20 overflow-hidden">
            {/* Header */}
            <div className="flex items-center p-4 border-b border-white/20 bg-white/30 backdrop-blur-md shadow-sm z-10">
                <div className="relative">
                    <div className={`p-0.5 rounded-full ${isGroup ? 'bg-linear-to-tr from-emerald-500 to-teal-500' : 'bg-linear-to-tr from-blue-500 to-indigo-500'} shadow-sm`}>
                        <Image
                            src={displayAvatar}
                            alt={displayName || ""}
                            fill
                            sizes="42px"
                            className="rounded-full object-cover border-2 border-white"
                            unoptimized
                        />
                    </div>
                    {!isGroup && (
                        <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full shadow-sm" />
                    )}
                </div>
                <div className="ml-3 flex-1">
                    <h2 className="font-bold text-gray-900 leading-tight tracking-tight">{displayName}</h2>
                    {isGroup ? (
                        <p className="text-[11px] font-medium text-teal-600/80 uppercase tracking-widest bg-teal-50 px-1.5 rounded-sm inline-block">
                            {t('group')}
                        </p>
                    ) : (
                        <p className="text-[11px] font-medium text-green-600/80 uppercase tracking-widest bg-green-50 px-1.5 rounded-sm inline-block">{t('online')}</p>
                    )}
                </div>
                {isGroup && onOpenGroupSettings && (
                    <button
                        onClick={onOpenGroupSettings}
                        className="p-2 rounded-xl hover:bg-gray-100/80 text-gray-400 hover:text-gray-600 transition-all"
                        title={t('group-settings')}
                    >
                        <Cog6ToothIcon className="w-5 h-5" />
                    </button>
                )}
            </div>

            {/* Chat History */}
            <div className="flex-1 overflow-y-auto p-6 space-y-4 bg-gray-50/30">
                {/* Intro spacing / avatar */}
                <div className="flex flex-col items-center justify-center my-8">
                    <Image
                        src={displayAvatar}
                        alt={displayName || ""}
                        width={80}
                        height={80}
                        className="rounded-full object-cover w-[80px] h-[80px] mb-3"
                        unoptimized
                        sizes="80px"
                    />
                    <h2 className="text-lg font-bold text-gray-800">{displayName}</h2>
                    <p className="text-sm text-gray-400">
                        {isGroup ? t('group-conversation') : t('select-chat')}
                    </p>
                </div>

                {messages.map((m, index) => {
                    const isMe = m.senderId === currentUid;
                    const isFirstInSequence = index === 0 || messages[index - 1].senderId !== m.senderId;
                    const isLastInSequence = index === messages.length - 1 || messages[index + 1].senderId !== m.senderId;

                    return (
                        <div
                            key={m.id}
                            className={`flex ${isMe ? "justify-end" : "justify-start"} ${!isLastInSequence ? "mb-1" : "mb-3"}`}
                        >
                            {!isMe && isLastInSequence ? (
                                    <Image
                                    src={isGroup
                                        ? (allUsers.find(u => u.email === m.senderId)?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(m.senderName || '?')}&background=random&color=fff&size=28`)
                                        : (otherUser.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(otherUser.lastName + ' ' + otherUser.firstName)}&background=6366f1&color=fff&bold=true`)
                                    }
                                    alt={m.senderName || ""}
                                    width={28}
                                    height={28}
                                    className="rounded-full self-start mt-1 mr-2 w-7 h-7 object-cover"
                                    unoptimized
                                    sizes="28px"
                                />
                            ) : (
                                !isMe && <div className="w-9 mr-2"></div>
                            )}

                            <div className={`flex flex-col max-w-[65%] group ${isMe ? "items-end" : "items-start"}`}>
                                {/* Sender name for group chats (other users only, first in sequence) */}
                                {isGroup && !isMe && isFirstInSequence && m.senderName && (
                                    <span className="text-[11px] font-semibold text-gray-500 mb-1 ml-1">
                                        {m.senderName}
                                    </span>
                                )}

                                {/* Text Message */}
                                {m.text && (
                                    <div
                                        className={`px-4 py-2.5 shadow-sm transition-all duration-200 ${isMe
                                            ? "bg-linear-to-br from-blue-600 to-indigo-600 text-white shadow-blue-200/50"
                                            : "bg-white/80 backdrop-blur-sm border border-white/50 text-gray-800 shadow-gray-200/30"
                                            } ${isMe
                                                ? `rounded-2xl ${isFirstInSequence ? 'rounded-tr-2xl' : 'rounded-tr-md'} ${isLastInSequence ? 'rounded-br-2xl' : 'rounded-br-md'} ml-auto`
                                                : `rounded-2xl ${isFirstInSequence ? 'rounded-tl-2xl' : 'rounded-tl-md'} ${isLastInSequence ? 'rounded-bl-2xl' : 'rounded-bl-md'} mr-auto`
                                            }`}
                                    >
                                        <p className="whitespace-pre-wrap wrap-break-word text-[14.5px] leading-relaxed font-normal">{m.text}</p>
                                    </div>
                                )}

                                <span className="text-[11px] text-gray-400 mt-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                    {formatMessageTime(m)}
                                </span>
                            </div>
                        </div>
                    );
                })}

                <div ref={endRef} className="h-4" />
            </div>

            {/* Input Area */}
            <div className="p-4 bg-white/50 backdrop-blur-md border-t border-white/20">
                <form onSubmit={handleSendMessage} className="flex items-center gap-3">
                    <div className="flex-1 bg-white/80 backdrop-blur-sm border border-white/50 rounded-2xl flex items-center px-4 shadow-sm group-focus-within:ring-2 group-focus-within:ring-blue-500/20 transition-all duration-200">
                        <input
                            type="text"
                            value={text}
                            onChange={(e) => setText(e.target.value)}
                            placeholder={t('type-message')}
                            className="w-full bg-transparent border-none focus:ring-0 text-gray-800 placeholder-gray-400 py-3 outline-none h-[48px] text-[15px]"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={!text.trim()}
                        className={`p-3 rounded-2xl transition-all duration-300 shadow-lg flex items-center justify-center ${text.trim()
                            ? "bg-linear-to-br from-blue-600 to-indigo-600 text-white scale-100 hover:scale-105 active:scale-95 shadow-blue-500/30"
                            : "bg-gray-100 text-gray-300 scale-95 shadow-none"
                            }`}
                    >
                        <PaperAirplaneIcon className="w-5 h-5" />
                    </button>
                </form>
            </div>
        </div>
    );
}
