'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname, useSearchParams } from 'next/navigation';
import { authApis, endpoints } from '@/lib/utils/api';
import MySpinner from '@/components/layout/MySpinner';
import ForumPost from '@/components/forums/ForumPost';
import { useTranslation } from 'react-i18next';
import { capitalizeFirstWord } from "@/lib/utils";

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
    updatedDate?: string;
    user: User;
}

interface Props {
    classroomId: string;
}

export default function ForumClient({ classroomId }: Props) {
    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [page, setPage] = useState<number>(1);
    const searchParams = useSearchParams();
    const router = useRouter();
    const pathname = usePathname();
    const { t } = useTranslation();

    const isAddPage = pathname.endsWith('/add-post');

    const toggleAddPost = () => {
        if (isAddPage) {
            router.push(`/classrooms/${classroomId}/forums`);
        } else {
            router.push(`/classrooms/${classroomId}/forums/add-post`);
        }
    };

    const loadPosts = async () => {
        try {
            setLoading(true);
            let url = `${endpoints['forum-posts'](classroomId)}?page=${page}`;

            const kw = searchParams.get('kw');
            if (kw) url += `&kw=${kw}`;

            const res = await authApis().get(url);
            console.info("Loaded posts:", res.data);

            if (page === 1) {
                setPosts(res.data.content);
            } else {
                setPosts(prev => [...prev, ...res.data.content]);
            }

            if (page >= res.data.totalPages) {
                setPage(0);
            }
        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    };

    const loadMore = () => {
        if (!loading && page > 0) setPage(page + 1);
    };

    const handlePostDeleted = (deletedId: string) => {
        setPosts(prev => prev.filter(p => p.id !== deletedId));
        alert(t("success-message"));
    };

    useEffect(() => {
        setPage(1);
        setPosts([]);
    }, [searchParams, classroomId]);

    useEffect(() => {
        if (page > 0) {
            loadPosts();
        }
    }, [page]);

    if (loading && posts.length === 0) return <MySpinner />;

    return (
        <div className="container mx-auto px-4 py-6 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <h3 className="text-2xl font-bold">
                    {t('list-posts')}
                </h3>
                <button
                    onClick={toggleAddPost}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                >
                    {isAddPage ? t('close') : `+ ${t('new-post')}`}
                </button>
            </div>

            {posts.length > 0 ? (
                <div className="grid md:grid-cols-2 gap-4">
                    {posts.map(post => (
                        <ForumPost
                            key={post.id}
                            classroomId={classroomId}
                            post={post}
                            onPostDeleted={handlePostDeleted}
                        />
                    ))}
                </div>
            ) : (
                <div className="bg-blue-50 border-l-4 border-blue-400 p-4 text-blue-700">
                    {capitalizeFirstWord(`${t("none")} ${t("post")}`)}
                </div>
            )}

            {loading && <MySpinner />}

            {page > 0 && (
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
    );
}
