'use client';

import { useContext, useEffect, useState } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { MyUserContext } from '@/lib/contexts/userContext';
import { checkPermission, checkCanEdit, formatVietnamTime } from '@/lib/utils';
import { FaPenToSquare, FaEye, FaTrashCan } from "react-icons/fa6";
import { authApis, endpoints } from "@/lib/utils/api";
import { useTranslation } from "react-i18next";

interface User {
    id: string;
    firstName: string;
    lastName: string;
    avatar: string;
}

interface Post {
    id: string;
    title: string;
    image?: string;
    createdDate: string;
    user: User;
}

interface ForumPostProps {
    post: Post;
    classRoomName: string;
    onPostDeleted: (id: string) => void;
}

export default function ForumPost({ post, classRoomName, onPostDeleted }: ForumPostProps) {
    const router = useRouter();
    const [perm, setPerm] = useState(false);
    const [canEditOrDelete, setCanEditOrDelete] = useState(false);
    const user = useContext(MyUserContext);
    const { t } = useTranslation();

    const handleDeletePost = async () => {
        if (!window.confirm("Bạn có chắc chắn muốn xoá bài đăng này?")) return;

        try {
            await authApis().delete(endpoints['forum-post-detail'](post.id));
            onPostDeleted(post.id);
        } catch (ex) {
            console.error("Delete error:", ex);
            alert("Xoá bài viết thất bại!");
        }
    };

    useEffect(() => {
        if (user?.id && checkPermission(post.user.id, user.id)) {
            setPerm(true);
            setCanEditOrDelete(checkCanEdit(post.createdDate));
        }
    }, [post.user.id, post.createdDate, user?.id]);

    return (
        <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="flex items-center bg-gray-50 p-4">
                <div className="relative w-12 h-12">
                    <Image
                        src={post.user.avatar}
                        alt={`${post.user.firstName} avatar`}
                        fill
                        className="rounded-full object-cover"
                    />
                </div>
                <div className="ml-3">
                    <div className="font-bold">
                        {post.user.firstName} {post.user.lastName}
                    </div>
                    <div className="text-sm text-gray-500">
                        {formatVietnamTime(post.createdDate)}
                    </div>
                </div>
            </div>

            <div className="p-4">
                <h3 className="text-xl font-semibold mb-4">{post.title}</h3>

                {post.image && (
                    <div className="relative w-full h-64 mb-4">
                        <Image
                            src={post.image}
                            alt={post.title}
                            fill
                            className="rounded-lg object-cover"
                        />
                    </div>
                )}

                <div className="flex justify-end space-x-2">
                    <button
                        onClick={() => router.push(`/classrooms/${post.id}/forums/${post.id}`)}
                        className="p-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors"
                        title={t('view')}
                    >
                        <FaEye size={20} />
                    </button>

                    {perm && (
                        <>
                            <button
                                onClick={() => router.push(`/classrooms/${post.id}/forums/${post.id}/edit`)}
                                disabled={!canEditOrDelete}
                                className="p-2 text-blue-600 hover:bg-blue-50 rounded-full transition-colors disabled:opacity-50"
                                title={t('edit-tooltip')}
                            >
                                <FaPenToSquare size={20} />
                            </button>

                            <button
                                onClick={handleDeletePost}
                                disabled={!canEditOrDelete}
                                className="p-2 text-red-600 hover:bg-red-50 rounded-full transition-colors disabled:opacity-50"
                                title={t('delete-tooltip')}
                            >
                                <FaTrashCan size={20} />
                            </button>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
