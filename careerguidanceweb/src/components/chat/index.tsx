'use client';

import { useEffect, useState, useRef } from "react";
import { useAppSelector } from "@/store/hooks";
import { useGetChatUsersQuery, type ApiUser as ApiUserDTO } from "@/store/features/api/apiSlice";
import Sidebar from "@/components/chat/Sidebar";
import ChatPanel from "@/components/chat/ChatPanel";
import GroupSettingsPanel from "@/components/chat/GroupSettingsPanel";
import { useFirebaseChat } from "@/hooks/useFirebaseChat";
import { useFirebaseGroupChat, GroupRoom } from "@/hooks/useFirebaseGroupChat";
import { XMarkIcon, PlusIcon } from "@heroicons/react/24/solid";
import Image from "next/image";
import { useTranslation } from "react-i18next";

// Re-export for downstream components
export type ApiUser = ApiUserDTO;

export default function ChatBox() {
    const userContext = useAppSelector((state) => state.auth.user);
    const user = userContext ? (userContext as unknown as ApiUser) : null;

    const firebaseChat = useFirebaseChat();
    const { firebaseUser, authenticateWithFirebase, chatRooms, openChat, activeChatId } = firebaseChat;
    const { t } = useTranslation();

    const groupChat = useFirebaseGroupChat(firebaseUser);
    const { groupRooms, groupMessages, activeGroupId, openGroup, closeGroup } = groupChat;

    const [searchQuery, setSearchQuery] = useState("");
    const [page, setPage] = useState(1);
    const [selectedOtherUser, setSelectedOtherUser] = useState<ApiUser | null>(null);

    // View: "direct" | "group"
    const [activeView, setActiveView] = useState<"direct" | "group">("direct");
    const [selectedGroup, setSelectedGroup] = useState<GroupRoom | null>(null);
    const [showGroupSettings, setShowGroupSettings] = useState(false);

    // Create group modal
    const [showCreateGroupModal, setShowCreateGroupModal] = useState(false);
    const [newGroupName, setNewGroupName] = useState("");
    const [selectedMembers, setSelectedMembers] = useState<ApiUser[]>([]);
    const [memberSearchQuery, setMemberSearchQuery] = useState("");

    // ── RTK Query: replaces fetchUsers + useCallback + 2 useEffects ──
    const { data: usersPage, isFetching: isLoadingMore } = useGetChatUsersQuery(
        { page, kw: searchQuery },
        { skip: !user } // Don't fetch until authenticated
    );

    // Derived: filter out self + admins from the merged list
    const allUsers: ApiUser[] = (usersPage?.content ?? []).filter(
        (u) => u.email !== user?.email && u.role !== "Admin"
    );
    const hasMore = !(usersPage?.last ?? true);

    // Authenticate with Firebase once
    useEffect(() => {
        if (user && !firebaseUser) {
            authenticateWithFirebase();
        }
    }, [user, firebaseUser, authenticateWithFirebase]);

    // Reset page when search changes
    useEffect(() => {
        setPage(1);
    }, [searchQuery]);

    const handleScrollEnd = () => {
        if (!isLoadingMore && hasMore) {
            setPage(prev => prev + 1);
        }
    };

    const handleSelectUser = async (otherUser: ApiUser) => {
        setSelectedOtherUser(otherUser);
        setActiveView("direct");
        setSelectedGroup(null);
        setShowGroupSettings(false);
        closeGroup();

        if (!user || !firebaseUser) return;

        const otherUid = otherUser.email;
        const existing = chatRooms.find(
            r => r.type === "direct" && r.members.includes(otherUid)
        );
        if (existing) {
            openChat(existing.id);
        } else {
            openChat(`TEMP_${otherUid}`);
        }
    };

    const handleSelectGroup = (group: GroupRoom) => {
        setActiveView("group");
        setSelectedGroup(group);
        setSelectedOtherUser(null);
        setShowGroupSettings(false);
        openGroup(group.id);
    };

    const handleCreateGroup = async () => {
        if (!newGroupName.trim() || selectedMembers.length === 0) return;

        try {
            const memberUids = selectedMembers.map(u => u.email);
            const groupId = await groupChat.createGroup(newGroupName.trim(), memberUids);

            // Reset modal
            setShowCreateGroupModal(false);
            setNewGroupName("");
            setSelectedMembers([]);
            setMemberSearchQuery("");

            // Open the newly created group
            // Wait a moment for Firestore listener to pick up the new group
            setTimeout(() => {
                const newGroup = groupChat.groupRooms.find(g => g.id === groupId);
                if (newGroup) {
                    handleSelectGroup(newGroup);
                } else {
                    // Group might not be in the list yet, open it directly
                    setActiveView("group");
                    openGroup(groupId);
                }
            }, 500);
        } catch (err) {
            console.error("Failed to create group:", err);
        }
    };

    const toggleMember = (u: ApiUser) => {
        setSelectedMembers(prev => {
            const exists = prev.find(m => m.email === u.email);
            if (exists) return prev.filter(m => m.email !== u.email);
            return [...prev, u];
        });
    };

    const filteredUsersForGroupModal = allUsers.filter(u =>
        `${u.lastName} ${u.firstName}`.toLowerCase().includes(memberSearchQuery.toLowerCase()) ||
        u.email.toLowerCase().includes(memberSearchQuery.toLowerCase())
    );

    // Update selectedGroup from live groupRooms
    useEffect(() => {
        if (selectedGroup) {
            const updated = groupRooms.find(g => g.id === selectedGroup.id);
            if (updated) {
                setSelectedGroup(updated);
            }
        }
    }, [groupRooms, selectedGroup]);

    if (!user) return <div className="p-8 text-center text-gray-500">{t('loading-user-data')}</div>;

    const currentUid = firebaseUser?.uid ?? user.email;
    const currentUserName = `${user.lastName} ${user.firstName}`;

    return (
        <div className="flex h-[calc(100vh-160px)] max-h-full bg-white/20 backdrop-blur-sm animate-in fade-in zoom-in duration-500">
            <Sidebar
                currentUserUid={currentUid}
                users={allUsers}
                chatRooms={chatRooms}
                activeChatId={activeChatId}
                onSelectUser={handleSelectUser}
                groupRooms={groupRooms}
                activeGroupId={activeGroupId}
                onSelectGroup={handleSelectGroup}
                onCreateGroup={() => setShowCreateGroupModal(true)}
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                isLoadingMore={isLoadingMore}
                hasMore={hasMore}
                onScrollEnd={handleScrollEnd}
                currentUserRole={user.role}
            />

            {/* Direct Chat */}
            {activeView === "direct" && activeChatId && selectedOtherUser && firebaseUser ? (
                <ChatPanel
                    currentUid={firebaseUser.uid}
                    activeChatId={activeChatId}
                    otherUser={selectedOtherUser}
                    firebaseChat={firebaseChat}
                    currentUserName={currentUserName}
                    allUsers={allUsers}
                />
            ) : activeView === "group" && activeGroupId && selectedGroup && firebaseUser ? (
                /* Group Chat */
                <>
                    <ChatPanel
                        currentUid={firebaseUser.uid}
                        activeChatId={activeGroupId}
                        otherUser={{ email: '', firstName: selectedGroup.groupName, lastName: '', role: '', avatar: '' }}
                        firebaseChat={firebaseChat}
                        isGroup={true}
                        groupName={selectedGroup.groupName}
                        groupAvatar={`https://ui-avatars.com/api/?name=${encodeURIComponent(selectedGroup.groupName)}&background=random&color=fff&bold=true`}
                        groupMessages={groupMessages}
                        groupChatHook={groupChat}
                        onOpenGroupSettings={() => setShowGroupSettings(prev => !prev)}
                        currentUserName={currentUserName}
                        allUsers={allUsers}
                    />
                    {showGroupSettings && (
                        <GroupSettingsPanel
                            group={selectedGroup}
                            currentUid={currentUid}
                            allUsers={allUsers}
                            onAddMember={groupChat.addMember}
                            onRemoveMember={groupChat.removeMember}
                            onDeleteGroup={async (gId) => {
                                await groupChat.deleteGroup(gId);
                                setActiveView("direct");
                                setSelectedGroup(null);
                                setShowGroupSettings(false);
                            }}
                            onClose={() => setShowGroupSettings(false)}
                            currentUserName={currentUserName}
                            currentUserAvatar={user?.avatar}
                        />
                    )}
                </>
            ) : (
                <div className="flex-1 flex flex-col items-center justify-center text-gray-400 bg-gray-50">
                    <svg className="w-16 h-16 mb-4 opacity-30" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M20 2H4a2 2 0 00-2 2v18l4-4h14a2 2 0 002-2V4a2 2 0 00-2-2z" />
                    </svg>
                    <p className="text-base font-medium">{t('select-to-chat')}</p>
                </div>
            )}

            {/* Create Group Modal */}
            {showCreateGroupModal && (
                <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md max-h-[80vh] flex flex-col overflow-hidden animate-in zoom-in-95 duration-200">
                        {/* Modal Header */}
                        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
                            <h3 className="text-lg font-bold text-gray-800">{t('create-group-title')}</h3>
                            <button
                                onClick={() => {
                                    setShowCreateGroupModal(false);
                                    setNewGroupName("");
                                    setSelectedMembers([]);
                                    setMemberSearchQuery("");
                                }}
                                className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-all"
                            >
                                <XMarkIcon className="w-5 h-5" />
                            </button>
                        </div>

                        {/* Modal Body */}
                        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
                            {/* Group Name */}
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
                                    {t('group-name-label')}
                                </label>
                                <input
                                    type="text"
                                    value={newGroupName}
                                    onChange={e => setNewGroupName(e.target.value)}
                                    placeholder={t('group-name-placeholder')}
                                    className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 transition-all"
                                    autoFocus
                                />
                            </div>

                            {/* Selected Members */}
                            {selectedMembers.length > 0 && (
                                <div>
                                    <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
                                        {t('selected-count', { count: selectedMembers.length })}
                                    </label>
                                    <div className="flex flex-wrap gap-2">
                                        {selectedMembers.map(m => (
                                            <span
                                                key={m.email}
                                                className="inline-flex items-center gap-1.5 bg-blue-50 text-blue-700 text-xs font-medium pl-2 pr-1 py-1 rounded-full"
                                            >
                                                {m.lastName} {m.firstName}
                                                <button
                                                    onClick={() => toggleMember(m)}
                                                    className="p-0.5 rounded-full hover:bg-blue-100 text-blue-400 hover:text-blue-600 transition-all"
                                                >
                                                    <XMarkIcon className="w-3 h-3" />
                                                </button>
                                            </span>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Member Search */}
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
                                    {t('add-member')}
                                </label>
                                <input
                                    type="text"
                                    value={memberSearchQuery}
                                    onChange={e => setMemberSearchQuery(e.target.value)}
                                    placeholder={t('search-users-placeholder')}
                                    className="w-full bg-gray-50 border border-gray-200 rounded-xl px-4 py-2.5 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 transition-all"
                                />
                            </div>

                            {/* User List */}
                            <div className="max-h-[250px] overflow-y-auto space-y-1 -mx-2">
                                {filteredUsersForGroupModal.slice(0, 20).map(u => {
                                    const isSelected = selectedMembers.some(m => m.email === u.email);
                                    return (
                                        <div
                                            key={u.email}
                                            onClick={() => toggleMember(u)}
                                            className={`flex items-center gap-3 px-3 py-2.5 rounded-xl cursor-pointer transition-all ${isSelected ? "bg-blue-50/80" : "hover:bg-gray-50"
                                                }`}
                                        >
                                            <Image
                                                src={u.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.lastName + ' ' + u.firstName)}&background=random&color=fff&bold=true`}
                                                alt={u.firstName}
                                                width={36}
                                                height={36}
                                                className="rounded-full object-cover w-9 h-9"
                                                unoptimized
                                            />
                                            <div className="flex-1 min-w-0">
                                                <p className="text-sm font-medium text-gray-800 truncate">
                                                    {u.lastName} {u.firstName}
                                                </p>
                                                <p className="text-xs text-gray-400 truncate">{u.email}</p>
                                            </div>
                                            {isSelected ? (
                                                <div className="w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center">
                                                    <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                                    </svg>
                                                </div>
                                            ) : (
                                                <div className="w-5 h-5 rounded-full border-2 border-gray-200" />
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Modal Footer */}
                        <div className="px-6 py-4 border-t border-gray-100 flex gap-3">
                            <button
                                onClick={() => {
                                    setShowCreateGroupModal(false);
                                    setNewGroupName("");
                                    setSelectedMembers([]);
                                    setMemberSearchQuery("");
                                }}
                                className="flex-1 py-2.5 px-4 bg-gray-100 text-gray-600 text-sm font-semibold rounded-xl hover:bg-gray-200 transition-all"
                            >
                                {t('cancel')}
                            </button>
                            <button
                                onClick={handleCreateGroup}
                                disabled={!newGroupName.trim() || selectedMembers.length === 0}
                                className="flex-1 flex items-center justify-center gap-2 py-2.5 px-4 bg-linear-to-r from-blue-500 to-indigo-500 text-white text-sm font-semibold rounded-xl shadow-md hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
                            >
                                <PlusIcon className="w-4 h-4" />
                                {t('create-group')}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
