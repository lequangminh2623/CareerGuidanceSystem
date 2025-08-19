'use client';

import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { authApis, endpoints } from '@/lib/utils/api';
import MySpinner from '@/components/layout/MySpinner';
import { useTranslation } from 'react-i18next';

interface Post {
    title?: string;
    content?: string;
    image?: string;
    [key: string]: string | undefined;
}

interface FieldErrors {
    [key: string]: string;
}

interface Props {
    classroomId: string;
}

export default function CreatePostClient({ classroomId }: Props) {
    const [post, setPost] = useState<Post>({});
    const imageRef = useRef<HTMLInputElement>(null);
    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const router = useRouter();
    const [previewImage, setPreviewImage] = useState<string | null>(post.image || null);
    const { t } = useTranslation();

    const setState = (value: string, field: string) => {
        setPost(prev => ({ ...prev, [field]: value }));
    };

    const validate = (): boolean => {
        const errors: FieldErrors = {};

        if (!post.title?.trim()) {
            errors.title = 'Tiêu đề không được để trống';
        }

        if (!post.content?.trim()) {
            errors.content = 'Nội dung không được để trống';
        }

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleAddPost = async (e: React.FormEvent) => {
        e.preventDefault();
        setMsg("");
        setFieldErrors({});

        if (validate()) {
            try {
                setLoading(true);
                const form = new FormData();

                Object.entries(post).forEach(([key, value]) => {
                    if (value) form.append(key, value);
                });

                if (imageRef.current?.files?.[0]) {
                    form.append("file", imageRef.current.files[0]);
                }

                const res = await authApis().post(
                    endpoints['forum-posts'](classroomId),
                    form,
                    { headers: { "Content-Type": "multipart/form-data" } }
                );

                router.push(`/classrooms/${classroomId}/forums?newPost=${encodeURIComponent(JSON.stringify(res.data))}`);
            } catch (ex: any) {
                if (ex.response?.status === 400 && Array.isArray(ex.response.data)) {
                    const errors: FieldErrors = {};
                    ex.response.data.forEach((err: { field: string; message: string }) => {
                        errors[err.field] = err.message;
                    });
                    setFieldErrors(errors);
                } else {
                    setMsg("Lỗi khi tạo bài đăng");
                }
            } finally {
                setLoading(false);
            }
        }
    };

    return (
        <div className="bg-white rounded-lg shadow-sm my-3">
            <div className="border-b px-6 py-4">
                <h3 className="text-xl font-semibold text-center">{t('new-post')}</h3>
            </div>

            <div className="p-6">
                {msg && (
                    <div className="mb-4 p-4 bg-red-50 border-l-4 border-red-500 text-red-700">
                        {msg}
                    </div>
                )}

                <form onSubmit={handleAddPost}>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t('title')}
                            </label>
                            <input
                                type="text"
                                value={post['title'] || ''}
                                onChange={(e) => setState(e.target.value, "title")}
                                placeholder={t('enter')}
                                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                    ${fieldErrors['title'] ? 'border-red-500' : 'border-gray-300'}`}
                            />
                            {fieldErrors['title'] && (
                                <p className="mt-1 text-sm text-red-500">{fieldErrors['title']}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t('content')}
                            </label>
                            <textarea
                                rows={5}
                                value={post['content'] || ''}
                                onChange={(e) => setState(e.target.value, "content")}
                                placeholder={t('enter')}
                                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                    ${fieldErrors['content'] ? 'border-red-500' : 'border-gray-300'}`}
                            />
                            {fieldErrors['content'] && (
                                <p className="mt-1 text-sm text-red-500">{fieldErrors['content']}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t('image')} ({t('optional')})
                            </label>
                            <input
                                ref={imageRef}
                                type="file"
                                accept="image/*"
                                onChange={(e) => {
                                    const file = e.target.files?.[0];
                                    if (file) {
                                        const url = URL.createObjectURL(file);
                                        setPreviewImage(url);
                                    }
                                }}
                                className="w-full px-3 py-2 border rounded-lg file:mr-4 file:py-2 file:px-4
                                         file:rounded-full file:border-0 file:text-sm file:font-semibold
                                         file:bg-primary file:text-white hover:file:bg-primary-dark"
                            />

                            {previewImage && (
                                <div className="mt-4 relative h-64">
                                    <Image
                                        src={previewImage}
                                        alt="Preview"
                                        fill
                                        className="rounded-lg object-contain"
                                    />
                                </div>
                            )}
                        </div>

                        <div>
                            {loading ? (
                                <MySpinner />
                            ) : (
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="w-full px-4 py-2 bg-primary text-white rounded-lg
                                             hover:bg-primary-dark transition-colors disabled:opacity-50"
                                >
                                    {t('do-post')}
                                </button>
                            )}
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
