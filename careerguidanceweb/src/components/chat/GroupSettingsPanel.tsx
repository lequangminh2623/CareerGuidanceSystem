'use client';

import { useState, useCallback } from "react";
import Image from "next/image";
import { ApiUser } from "@/components/chat/index";
import { GroupRoom } from "@/hooks/useFirebaseGroupChat";
import { XMarkIcon, PlusIcon, TrashIcon, UserMinusIcon, MagnifyingGlassIcon } from "@heroicons/react/24/solid";
import { useTranslation } from "react-i18next";

interface GroupSettingsPanelProps {
    group: GroupRoom;
    currentUid: string;
    allUsers: ApiUser[];
    onAddMember: (groupId: string, userUid: string) => Promise<void>;
    onRemoveMember: (groupId: string, userUid: string) => Promise<void>;
    onDeleteGroup: (groupId: string) => Promise<void>;
    onClose: () => void;
    currentUserName?: string;
    currentUserAvatar?: string;
}

function getGroupAvatar(groupName: string): string {
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(groupName)}&background=random&color=fff&bold=true&size=80`;
}

export default function GroupSettingsPanel({
    group,
    currentUid,
    allUsers,
    onAddMember,
    onRemoveMember,
    onDeleteGroup,
    onClose,
    currentUserName,
    currentUserAvatar,
}: GroupSettingsPanelProps) {
    const { t } = useTranslation();
    const isAdmin = group.adminId === currentUid;
    const [addSearch, setAddSearch] = useState("");
    const [isDeleting, setIsDeleting] = useState(false);

    // Members info: match UIDs to API users
    const memberUsers = group.members.map(uid => {
        const apiUser = allUsers.find(u => u.email === uid);
        return {
            uid,
            name: uid === currentUid && currentUserName ? currentUserName : (apiUser ? `${apiUser.lastName} ${apiUser.firstName}` : uid),
            avatar: uid === currentUid && currentUserAvatar ? currentUserAvatar : (apiUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(apiUser ? apiUser.lastName + ' ' + apiUser.firstName : uid)}&background=random&color=fff&bold=true`),
            isAdmin: uid === group.adminId,
        };
    });

    // Users not in group, for add member search
    const nonMembers = allUsers.filter(
        u => !group.members.includes(u.email)
    );
    const filteredNonMembers = nonMembers.filter(u =>
        `${u.lastName} ${u.firstName}`.toLowerCase().includes(addSearch.toLowerCase()) ||
        u.email.toLowerCase().includes(addSearch.toLowerCase())
    );

    const handleAddMember = useCallback(async (user: ApiUser) => {
        try {
            await onAddMember(group.id, user.email);
            setAddSearch("");
        } catch (err) {
            console.error(t('error-message'), err);
        }
    }, [group.id, onAddMember, t]);

    const handleRemoveMember = useCallback(async (uid: string) => {
        if (uid === group.adminId) return; // Can't remove admin
        try {
            await onRemoveMember(group.id, uid);
        } catch (err) {
            console.error(t('error-message'), err);
        }
    }, [group.id, group.adminId, onRemoveMember, t]);

    const handleDeleteGroup = useCallback(async () => {
        const confirmed = window.confirm(t('delete-group-confirm', { name: group.groupName }));
        if (!confirmed) return;

        setIsDeleting(true);
        try {
            await onDeleteGroup(group.id);
            onClose();
        } catch (err) {
            console.error(t('error-message'), err);
            setIsDeleting(false);
        }
    }, [group.id, group.groupName, onDeleteGroup, onClose, t]);

    return (
        <div className="flex flex-col w-[300px] h-full bg-white/80 backdrop-blur-md border-l border-white/20 overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-gray-100/50">
                <h3 className="font-bold text-gray-800">{t('group-info')}</h3>
                <button
                    onClick={onClose}
                    className="p-1.5 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-all"
                >
                    <XMarkIcon className="w-5 h-5" />
                </button>
            </div>

            <div className="flex-1 overflow-y-auto">
                {/* Group Info */}
                <div className="flex flex-col items-center py-6 border-b border-gray-100/50">
                    <Image
                        src={getGroupAvatar(group.groupName)}
                        alt={group.groupName}
                        width={80}
                        height={80}
                        className="rounded-full object-cover w-20 h-20 shadow-md mb-3"
                        unoptimized
                    />
                    <h2 className="text-lg font-bold text-gray-800">{group.groupName}</h2>
                    <p className="text-xs text-gray-400 mt-1">{group.members.length} {t('members').toLowerCase()}</p>
                </div>

                {/* Members List */}
                <div className="px-4 py-3">
                    <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                        {t('members')} ({group.members.length})
                    </h4>
                    <div className="space-y-1">
                        {memberUsers.map(m => (
                            <div
                                key={m.uid}
                                className="flex items-center gap-3 py-2 px-2 rounded-xl hover:bg-gray-50/80 transition-all group/member"
                            >
                                <Image
                                    src={m.avatar}
                                    alt={m.name}
                                    width={36}
                                    height={36}
                                    className="rounded-full object-cover w-9 h-9"
                                    unoptimized
                                />
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-medium text-gray-800 truncate">
                                        {m.name}
                                        {m.uid === currentUid && (
                                            <span className="text-gray-400 text-xs ml-1">{t('you-parentheses')}</span>
                                        )}
                                    </p>
                                    {m.isAdmin && (
                                        <span className="text-[10px] font-semibold text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded-md">
                                            Admin
                                        </span>
                                    )}
                                </div>
                                {isAdmin && !m.isAdmin && (
                                    <button
                                        onClick={() => handleRemoveMember(m.uid)}
                                        className="opacity-0 group-hover/member:opacity-100 p-1.5 rounded-lg hover:bg-red-50 text-gray-300 hover:text-red-500 transition-all"
                                        title={t('remove-member')}
                                    >
                                        <UserMinusIcon className="w-4 h-4" />
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Add Member (Admin Only) */}
                {isAdmin && (
                    <div className="px-4 py-3 border-t border-gray-100/50">
                        <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">
                            {t('add-member')}
                        </h4>
                        <div className="relative mb-3">
                            <MagnifyingGlassIcon className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
                            <input
                                type="text"
                                value={addSearch}
                                onChange={e => setAddSearch(e.target.value)}
                                placeholder={t('find-users-placeholder')}
                                className="w-full bg-gray-50 border border-gray-200 rounded-lg pl-9 pr-3 py-2 text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-400 transition-all"
                            />
                        </div>
                        {addSearch && (
                            <div className="max-h-[200px] overflow-y-auto space-y-1">
                                {filteredNonMembers.length === 0 ? (
                                    <p className="text-center text-gray-400 text-xs py-3">{t('not-found')}</p>
                                ) : (
                                    filteredNonMembers.slice(0, 10).map(u => (
                                        <div
                                            key={u.email}
                                            onClick={() => handleAddMember(u)}
                                            className="flex items-center gap-3 py-2 px-2 rounded-xl hover:bg-blue-50/80 cursor-pointer transition-all"
                                        >
                                            <Image
                                                src={u.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.lastName + ' ' + u.firstName)}&background=random&color=fff&bold=true`}
                                                alt={u.firstName}
                                                width={32}
                                                height={32}
                                                className="rounded-full object-cover w-8 h-8"
                                                unoptimized
                                            />
                                            <p className="text-sm text-gray-700 truncate flex-1">
                                                {u.lastName} {u.firstName}
                                            </p>
                                            <PlusIcon className="w-4 h-4 text-blue-500" />
                                        </div>
                                    ))
                                )}
                            </div>
                        )}
                    </div>
                )}

                {/* Delete Group (Admin Only) */}
                {isAdmin && (
                    <div className="px-4 py-4 border-t border-gray-100/50">
                        <button
                            onClick={handleDeleteGroup}
                            disabled={isDeleting}
                            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-red-50 text-red-600 text-sm font-semibold rounded-xl hover:bg-red-100 active:scale-[0.98] transition-all disabled:opacity-50"
                        >
                            <TrashIcon className="w-4 h-4" />
                            {isDeleting ? t('deleting') : t('delete-group')}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
