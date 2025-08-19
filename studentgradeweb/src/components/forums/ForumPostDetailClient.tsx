'use client';

import { useContext, useEffect, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { MyUserContext } from '@/lib/contexts/userContext';
import MySpinner from '@/components/layout/MySpinner';
import { authApis, endpoints } from '@/lib/utils/api';
import ForumReply from '@/components/forums/ForumReply';
import { checkPermission, checkCanEdit, formatVietnamTime, capitalizeFirstWord } from '@/lib/utils';
import { FaPenToSquare, FaTrashCan } from 'react-icons/fa6';
import { useTranslation } from 'react-i18next';

interface User {
    id: string;
    firstName: string;
    lastName: string;
    avatar: string;
}

interface Reply {
    id: string;
    content: string;
    createdDate: string;
    user: User;
}

interface Post {
    id: string;
    title: string;
    content: string;
    image?: string;
    createdDate: string;
    user: User;
    forumReplies: Reply[];
}

interface Props {
    classroomId: string;
    postId: string;
}

export default function ForumPostDetailClient({ classroomId, postId }: Props) {
    const [post, setPost] = useState<Post | null>(null);
    const [replies, setReplies] = useState<Reply[]>([]);
    const [perm, setPerm] = useState(false);
    const [canEditOrDelete, setCanEditOrDelete] = useState(false);
    const user = useContext(MyUserContext);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);
    const router = useRouter();
    const pathname = usePathname();
    const { t } = useTranslation();

    const isAddPage = pathname.endsWith('add');

    const loadForumPost = async () => {
        try {
            setLoading(true);
            const url = `${endpoints['forum-post-detail'](postId)}?page=${page}`;
            const res = await authApis().get(url);

            setPost(res.data.content);
            setReplies((prev) => [...prev, ...res.data.content.forumReplies]);

            if (page >= res.data.totalPages) {
                setPage(0);
            }
        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    };

    const handleDeletePost = async () => {
        if (!post) return;
        if (!window.confirm(t('confirm-delete-post'))) return;

        try {
            await authApis().delete(endpoints['forum-post-detail'](post.id));
            alert(t('delete-success'));
            router.push(`/classrooms/${classroomId}/forums`);
        } catch (ex) {
            console.error('Delete error:', ex);
            alert(t('delete-failed'));
        }
    };

    const handleReplyDeleted = (deletedId: string) => {
        setReplies((prev) => prev.filter((r) => r.id !== deletedId));
        alert(t('reply-delete-success'));
    };

    useEffect(() => {
        if (post && user?.id) {
            if (checkPermission(post.user.id, user.id)) {
                setPerm(true);
                setCanEditOrDelete(checkCanEdit(post.createdDate));
            }
        }
    }, [post, user]);

    const loadMore = () => {
        if (!loading && page > 0) setPage((p) => p + 1);
    };

    useEffect(() => {
        setPost(null);
        setReplies([]);
        setPage(1);
    }, [postId]);

    useEffect(() => {
        if (page > 0) loadForumPost();
    }, [page]);

    if (loading && !post) return <MySpinner />;

    return (
        <div className="container mx-auto px-4 py-6 min-h-screen">
            <h3 className="text-2xl font-bold mb-6">{t('post-detail')}</h3>

            {post && (
                <>
                    <div className="bg-white rounded-lg shadow-sm overflow-hidden">
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

                        <div className="p-6">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-semibold">{post.title}</h2>

                                {perm && (
                                    <div className="flex space-x-2">
                                        {canEditOrDelete && (
                                            <button
                                                onClick={() => router.push(`/classrooms/${classroomId}/forums/${postId}/edit`)}
                                                className="px-3 py-1 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 flex items-center gap-1"
                                            >
                                                <FaPenToSquare /> {t('edit')}
                                            </button>
                                        )}
                                        <button
                                            onClick={handleDeletePost}
                                            className="px-3 py-1 bg-red-600 text-white rounded-lg hover:bg-red-700 flex items-center gap-1"
                                        >
                                            <FaTrashCan /> {t('delete')}
                                        </button>
                                    </div>
                                )}
                            </div>

                            {post.image && (
                                <div className="my-4">
                                    <Link href={post.image} target="_blank">
                                        <div className="relative h-64 w-full">
                                            <Image
                                                src={post.image}
                                                alt={post.title}
                                                fill
                                                className="rounded-lg object-contain"
                                            />
                                        </div>
                                    </Link>
                                </div>
                            )}

                            <p className="px-5 text-gray-700 whitespace-pre-line">{post.content}</p>
                        </div>
                    </div>

                    <div className="mt-8">
                        <div className="flex justify-between items-center mb-6">
                            <h5 className="text-xl font-semibold">{t('reply')}</h5>
                            <button
                                onClick={() =>
                                    router.push(
                                        isAddPage
                                            ? `/classrooms/${classroomId}/forums/${postId}`
                                            : `/classrooms/${classroomId}/forums/${postId}/add`
                                    )
                                }
                                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                            >
                                {isAddPage ? t('close') : `+ ${t('new-reply')}`}
                            </button>
                        </div>

                        <div className="space-y-4">
                            {replies.length > 0 ? (
                                replies.map((reply) => (
                                    <ForumReply
                                        key={reply.id}
                                        classroomId={classroomId}
                                        postId={postId}
                                        reply={reply}
                                        onReplyDeleted={handleReplyDeleted}
                                    />
                                ))
                            ) : (
                                <div className="text-center text-gray-500">
                                    {capitalizeFirstWord(`${t('no')} ${t('reply')}`)}
                                </div>
                            )}
                        </div>

                        {loading && <MySpinner />}

                        {page > 0 && !loading && (
                            <div className="text-center mt-6">
                                <button
                                    onClick={loadMore}
                                    className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                                >
                                    {t('more')}
                                </button>
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}
