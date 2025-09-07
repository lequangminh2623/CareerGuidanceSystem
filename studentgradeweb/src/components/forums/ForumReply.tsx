'use client';

import { useContext, useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { MyUserContext } from "@/lib/contexts/userContext";
import { checkPermission, checkCanEdit } from '@/lib/utils';
import { FaPenToSquare, FaTrashCan, FaReply } from "react-icons/fa6";
import { IoCloseSharp } from "react-icons/io5";
import { authApis, endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";
import TimeConvert from "../layout/TimeConvert";

interface User {
    id: string;
    firstName: string;
    lastName: string;
    avatar: string;
}

interface Reply {
    id: string;
    content: string;
    image?: string;
    createdDate: string;
    updatedDate?: string;
    user: User;
}

interface ForumReplyProps {
    classroomId: string;
    postId: string;
    reply: Reply;
    onReplyDeleted: (id: string) => void;
}

export default function ForumReply({ classroomId, postId, reply, onReplyDeleted }: ForumReplyProps) {
    const [perm, setPerm] = useState(false);
    const [canEditOrDelete, setCanEditOrDelete] = useState(false);
    const [showChildren, setShowChildren] = useState(false);
    const [childReplies, setChildReplies] = useState<Reply[]>([]);
    const [loadingChildren, setLoadingChildren] = useState(false);
    const [loadedOnce, setLoadedOnce] = useState(false);
    const [page, setPage] = useState(1);

    const user = useContext(MyUserContext);
    const router = useRouter();
    const pathname = usePathname();
    const { t } = useTranslation();

    const isAddPage = pathname.endsWith(`/replies/${reply.id}/add-reply`);

    const handleDeleteReply = async () => {
        if (!window.confirm(t('confirm-remove'))) return;

        try {
            await authApis().delete(endpoints['forum-reply-detail'](postId, reply.id));
            onReplyDeleted(reply.id);
        } catch (ex) {
            console.error("Delete error:", ex);
            alert(t('error-message'));
        }
    };

    const loadChildReplies = async () => {
        try {
            setLoadingChildren(true);

            const url = `${endpoints['forum-child-replies'](postId, reply.id)}?page=${page}`;
            const res = await authApis().get(url);

            setChildReplies((prev) => [...prev, ...res.data.content]);

            if (page >= res.data.totalPages) {
                setPage(0);
            }
        } catch (err) {
            console.error("Failed to load child replies:", err);
        } finally {
            setLoadingChildren(false);
        }
    };

    const toggleChildren = async () => {
        setShowChildren((prev) => !prev);
        if (!loadedOnce) {
            await loadChildReplies();
            setLoadedOnce(true);
        }
    };

    const loadMore = () => {
        if (!loadingChildren && page > 0) setPage(page + 1);
    };

    const toggleAddReply = () => {
        if (isAddPage) {
            router.push(`/classrooms/${classroomId}/forums/${postId}`);
        } else {
            router.push(`/classrooms/${classroomId}/forums/${postId}/replies/${reply.id}/add-reply`);
        }
    };

    const handleChildReplyDeleted = (deletedId: string) => {
        setChildReplies((prev) => prev.filter((child) => child.id !== deletedId));
    };

    useEffect(() => {
        setPage(1);
        setChildReplies([]);
    }, [postId, reply.id]);

    useEffect(() => {
        if (page > 0 && loadedOnce) {
            loadChildReplies();
        }
    }, [page]);

    useEffect(() => {
        if (!user) return;
        if (checkPermission(reply.user.id, user.id)) {
            setPerm(true);
            setCanEditOrDelete(checkCanEdit(reply.createdDate));
        }
    }, [reply.user.id, reply.createdDate, user?.id]);

    return (
        <div className="bg-white rounded-lg shadow-sm my-3 overflow-hidden">
            <div className="flex items-center bg-gray-50 p-4">
                <div className="relative w-12 h-12">
                    <Image
                        src={reply.user.avatar}
                        alt={`${reply.user.firstName} avatar`}
                        fill
                        className="rounded-full object-cover"
                    />
                </div>
                <div className="ml-3">
                    <div className="font-bold">
                        {reply.user.firstName} {reply.user.lastName}
                    </div>
                    <div className="text-sm text-gray-500">
                        <TimeConvert timestamp={reply.createdDate} />
                        {reply.updatedDate && (
                            <span className="ml-2 text-xs italic">
                                ({t('edited')})
                            </span>
                        )}
                    </div>
                </div>
            </div>

            <div className="p-4">
                <div className="flex justify-end mb-4">
                    {perm && (
                        <div className="flex space-x-2">
                            <button
                                onClick={() =>
                                    router.push(`/classrooms/${classroomId}/forums/${postId}/replies/${reply.id}/edit-reply`)
                                }
                                disabled={!canEditOrDelete}
                                className="p-2 text-blue-600 hover:bg-blue-50 rounded-full transition-colors disabled:opacity-50"
                                title={t("edit-tooltip")}
                            >
                                <FaPenToSquare size={20} />
                            </button>
                            <button
                                onClick={handleDeleteReply}
                                disabled={!canEditOrDelete}
                                className="p-2 text-red-600 hover:bg-red-50 rounded-full transition-colors disabled:opacity-50"
                                title={t("delete-tooltip")}
                            >
                                <FaTrashCan size={20} />
                            </button>
                        </div>
                    )}
                </div>

                {reply.image && (
                    <div className="my-4">
                        <Link href={reply.image} target="_blank">
                            <div className="relative h-48 w-full">
                                <Image
                                    src={reply.image}
                                    alt="Reply image"
                                    fill
                                    className="rounded-lg object-contain"
                                />
                            </div>
                        </Link>
                    </div>
                )}

                <p className="px-5 text-gray-700">{reply.content}</p>

                <div className="mt-4 space-x-2">
                    <button
                        onClick={toggleChildren}
                        className="px-3 py-1 text-primary border border-primary rounded hover:bg-primary-50 transition-colors"
                    >
                        {showChildren ? t("hide-replies") : t("view-replies")}
                    </button>

                    <button
                        onClick={toggleAddReply}
                        className="p-2 text-primary hover:bg-primary-50 rounded-full transition-colors"
                    >
                        {isAddPage ? <IoCloseSharp size={23} /> : <FaReply size={22} />}
                    </button>
                </div>

                {showChildren && (
                    <div className="mt-4 ml-4">
                        {loadingChildren ? (
                            <MySpinner />
                        ) : (
                            <div className="space-y-4">
                                {childReplies.length > 0 ? (
                                    childReplies.map((child) => (
                                        <ForumReply
                                            key={child.id}
                                            classroomId={classroomId}
                                            postId={postId}
                                            reply={child}
                                            onReplyDeleted={handleChildReplyDeleted}
                                        />
                                    ))
                                ) : (
                                    <div className="text-gray-500">
                                        {capitalizeFirstWord(`${t("none")} ${t("reply")}`)}
                                    </div>
                                )}

                                {page > 0 && (
                                    <div className="text-center">
                                        <button
                                            onClick={loadMore}
                                            className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                                        >
                                            {capitalizeFirstWord(`${t("more")} ${t("reply")}`)}
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
