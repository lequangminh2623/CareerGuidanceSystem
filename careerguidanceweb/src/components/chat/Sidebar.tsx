'use client';

import { useState } from "react";
import Image from "next/image";
import { ChatRoom } from "@/hooks/useFirebaseChat";
import { GroupRoom } from "@/hooks/useFirebaseGroupChat";
import { ApiUser } from "@/components/chat/index";
import { PlusIcon, UserGroupIcon, ChatBubbleLeftRightIcon } from "@heroicons/react/24/solid";
import { useTranslation } from "react-i18next";
import moment from "moment";

interface SidebarProps {
    currentUserUid: string;
    users: ApiUser[];
    chatRooms: ChatRoom[];
    activeChatId: string | null;
    onSelectUser: (user: ApiUser) => void;

    // Group props
    groupRooms: GroupRoom[];
    activeGroupId: string | null;
    onSelectGroup: (group: GroupRoom) => void;
    onCreateGroup: () => void;

    searchQuery: string;
    setSearchQuery: (query: string) => void;
    isLoadingMore: boolean;
    hasMore: boolean;
    onScrollEnd: () => void;
    currentUserRole?: string;
}

/** Find a direct chatRoom for this user, matching by Firebase UID */
function getRoomForUser(userUid: string, currentUid: string, chatRooms: ChatRoom[]): ChatRoom | undefined {
    return chatRooms.find(
        r => r.type === "direct" && r.members.includes(userUid) && r.members.includes(currentUid)
    );
}

function timeAgo(timestamp: { toDate?: () => Date; seconds?: number } | null | undefined, locale: string): string {
    if (!timestamp) return "";
    const date = timestamp.toDate ? timestamp.toDate() : new Date((timestamp.seconds ?? 0) * 1000);
    moment.locale(locale);
    return moment(date).fromNow(true); // short version (e.g. 1m, 1h) depends on moment locale config but we'll use fromNow
}

function getGroupAvatar(groupName: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(groupName)}&background=random&color=fff&bold=true`;
}

export default function Sidebar({
    currentUserUid,
    users,
    chatRooms,
    activeChatId,
    onSelectUser,
    groupRooms,
    activeGroupId,
    onSelectGroup,
    onCreateGroup,
    searchQuery,
    setSearchQuery,
    isLoadingMore,
    hasMore,
    onScrollEnd,
    currentUserRole,
}: SidebarProps) {
    const { t, i18n } = useTranslation();
    const [activeTab, setActiveTab] = useState<"personal" | "group">("personal");

    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        if (activeTab !== "personal") return;
        const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
        if (scrollTop + clientHeight >= scrollHeight - 80 && !isLoadingMore && hasMore) {
            onScrollEnd();
        }
    };

    // Filter groups by search query
    const filteredGroups = groupRooms.filter(g =>
        g.groupName.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="flex flex-col w-[320px] h-full max-h-full bg-white/60 backdrop-blur-md border-r border-white/20 overflow-hidden shadow-sm">
            {/* Header */}
            <div className="px-4 py-5 border-b border-gray-100/50">
                <div className="flex items-center justify-between mb-4 px-1">
                    <h2 className="text-xl font-bold bg-linear-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent">{t('chat')}</h2>
                </div>
                <div className="relative group">
                    <input
                        type="text"
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                        placeholder={activeTab === "personal" ? t("search-conversations-placeholder") : t("search-groups-placeholder")}
                        className="w-full bg-gray-100/50 border border-transparent rounded-xl px-4 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:bg-white focus:border-blue-500/30 transition-all duration-200"
                    />
                </div>
            </div>

            {/* Tabs */}
            <div className="flex border-b border-gray-100/50">
                <button
                    onClick={() => setActiveTab("personal")}
                    className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-semibold transition-all duration-200 relative ${activeTab === "personal"
                        ? "text-blue-600"
                        : "text-gray-400 hover:text-gray-600"
                        }`}
                >
                    <ChatBubbleLeftRightIcon className="w-4 h-4" />
                    {t('personal')}
                    {activeTab === "personal" && (
                        <div className="absolute bottom-0 left-2 right-2 h-0.5 bg-linear-to-r from-blue-500 to-indigo-500 rounded-full" />
                    )}
                </button>
                <button
                    onClick={() => setActiveTab("group")}
                    className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-semibold transition-all duration-200 relative ${activeTab === "group"
                        ? "text-blue-600"
                        : "text-gray-400 hover:text-gray-600"
                        }`}
                >
                    <UserGroupIcon className="w-4 h-4" />
                    {t('group')}
                    {/* Unread group badge */}
                    {groupRooms.some(g => g.seenBy && !g.seenBy.includes(currentUserUid) && g.lastMessage) && (
                        <span className="w-2 h-2 bg-blue-500 rounded-full" />
                    )}
                    {activeTab === "group" && (
                        <div className="absolute bottom-0 left-2 right-2 h-0.5 bg-linear-to-r from-blue-500 to-indigo-500 rounded-full" />
                    )}
                </button>
            </div>

            {/* Content */}
            <div className="flex-1 min-h-0 overflow-y-auto" onScroll={handleScroll}>
                {activeTab === "personal" ? (
                    /* ======== Personal Tab ======== */
                    <>
                        {users.length === 0 && (
                            <p className="text-center text-gray-400 text-sm py-8">{t('no-users-found')}</p>
                        )}
                        {[...users]
                            .sort((a, b) => {
                                const roomA = getRoomForUser(a.email, currentUserUid, chatRooms);
                                const roomB = getRoomForUser(b.email, currentUserUid, chatRooms);
                                const tA = roomA?.lastMessageAt?.toMillis?.() ?? 0;
                                const tB = roomB?.lastMessageAt?.toMillis?.() ?? 0;
                                return tB - tA;
                            })
                            .map(u => {
                                const userUid = u.email;
                                const room = getRoomForUser(userUid, currentUserUid, chatRooms);
                                const isActive = room && activeChatId === room.id;
                                const hasChat = !!room?.lastMessageAt;
                                const isUnread = hasChat &&
                                    room?.lastSenderId !== currentUserUid &&
                                    (!room?.seenBy || !room.seenBy.includes(currentUserUid));
                                const isMe = room?.lastSenderId === currentUserUid;
                                const preview = room?.lastMessage
                                    ? (isMe ? `${t('you')}: ${room.lastMessage}` : room.lastMessage)
                                    : null;
                                const timeStr = timeAgo(room?.lastMessageAt, i18n.language);

                                return (
                                    <div
                                        key={u.email}
                                        onClick={() => onSelectUser(u)}
                                        className={`relative flex items-center gap-3 px-4 py-4 cursor-pointer transition-all duration-200 group ${isActive
                                            ? "bg-blue-50/80 backdrop-blur-sm"
                                            : "hover:bg-gray-50/50"
                                            }`}
                                    >
                                        {isActive && (
                                            <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-r-full" />
                                        )}
                                        <div className="relative shrink-0">
                                            <div className={`p-0.5 rounded-full ${isActive ? 'ring-2 ring-blue-500/20' : 'group-hover:ring-2 group-hover:ring-gray-200'}`}>
                                                <Image
                                                    src={u.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.lastName + ' ' + u.firstName)}&background=random&color=fff&bold=true`}
                                                    alt={u.firstName}
                                                    width={48}
                                                    height={48}
                                                    className="rounded-full object-cover w-12 h-12 shadow-sm"
                                                    unoptimized
                                                />
                                            </div>
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center justify-between">
                                                <p className={`text-sm truncate ${isUnread ? "font-bold text-gray-900" : "font-medium text-gray-800"}`}>
                                                    {u.lastName} {u.firstName}
                                                </p>
                                                {timeStr && (
                                                    <span className={`text-xs shrink-0 ml-2 ${isUnread ? "text-blue-600 font-semibold" : "text-gray-400"}`}>
                                                        {timeStr}
                                                    </span>
                                                )}
                                            </div>
                                            <div className="flex items-center justify-between mt-0.5">
                                                <p className={`text-xs truncate ${isUnread ? "font-semibold text-gray-700" : "text-gray-400"}`}>
                                                    {preview ?? t("no-messages-yet")}
                                                </p>
                                                {isUnread && (
                                                    <span className="w-2.5 h-2.5 bg-blue-500 rounded-full shrink-0 ml-2" />
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}

                        {isLoadingMore && (
                            <div className="flex items-center justify-center gap-2 py-4">
                                <span className="w-4 h-4 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin" />
                                <p className="text-gray-400 text-xs">{t('loading-more')}</p>
                            </div>
                        )}
                        {!isLoadingMore && !hasMore && users.length > 0 && (
                            <p className="text-center text-gray-300 text-xs py-3">{t('end-of-list')}</p>
                        )}
                    </>
                ) : (
                    /* ======== Group Tab ======== */
                    <>
                        {/* Create Group Button - only for Teachers */}
                        {currentUserRole !== 'Student' && (
                            <div className="px-4 py-3">
                                <button
                                    onClick={onCreateGroup}
                                    className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-linear-to-r from-blue-500 to-indigo-500 text-white text-sm font-semibold rounded-xl shadow-md hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] transition-all duration-200"
                                >
                                    <PlusIcon className="w-4 h-4" />
                                    {t('new-group')}
                                </button>
                            </div>
                        )}

                        {filteredGroups.length === 0 && (
                            <p className="text-center text-gray-400 text-sm py-8">{t('no-groups')}</p>
                        )}

                        {filteredGroups.map(group => {
                            const isActive = activeGroupId === group.id;
                            const isUnread = group.lastMessage &&
                                group.seenBy &&
                                !group.seenBy.includes(currentUserUid);
                            const isMe = group.lastSenderId === currentUserUid;
                            const preview = group.lastMessage
                                ? (isMe ? `${t('you')}: ${group.lastMessage}` : group.lastMessage)
                                : null;
                            const timeStr = timeAgo(group.lastMessageAt, i18n.language);

                            return (
                                <div
                                    key={group.id}
                                    onClick={() => onSelectGroup(group)}
                                    className={`relative flex items-center gap-3 px-4 py-4 cursor-pointer transition-all duration-200 group ${isActive
                                        ? "bg-blue-50/80 backdrop-blur-sm"
                                        : "hover:bg-gray-50/50"
                                        }`}
                                >
                                    {isActive && (
                                        <div className="absolute left-0 top-0 bottom-0 w-1 bg-blue-500 rounded-r-full" />
                                    )}
                                    {/* Group Avatar */}
                                    <div className="relative shrink-0">
                                        <div className={`p-0.5 rounded-full ${isActive ? 'ring-2 ring-blue-500/20' : 'group-hover:ring-2 group-hover:ring-gray-200'}`}>
                                            <Image
                                                src={getGroupAvatar(group.groupName)}
                                                alt={group.groupName}
                                                width={48}
                                                height={48}
                                                className="rounded-full object-cover w-12 h-12 shadow-sm"
                                                unoptimized
                                            />
                                        </div>
                                    </div>
                                    {/* Group Info */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center justify-between">
                                            <p className={`text-sm truncate ${isUnread ? "font-bold text-gray-900" : "font-medium text-gray-800"}`}>
                                                {group.groupName}
                                            </p>
                                            {timeStr && (
                                                <span className={`text-xs shrink-0 ml-2 ${isUnread ? "text-blue-600 font-semibold" : "text-gray-400"}`}>
                                                    {timeStr}
                                                </span>
                                            )}
                                        </div>
                                        <div className="flex items-center justify-between mt-0.5">
                                            <p className={`text-xs truncate ${isUnread ? "font-semibold text-gray-700" : "text-gray-400"}`}>
                                                {preview ?? t("no-messages-yet")}
                                            </p>
                                            {isUnread && (
                                                <span className="w-2.5 h-2.5 bg-blue-500 rounded-full shrink-0 ml-2" />
                                            )}
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </>
                )}
            </div>
        </div>
    );
}
