'use client';

import { useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Image from 'next/image';
import { authApis, endpoints } from '@/lib/utils/api';
import MySpinner from '@/components/layout/MySpinner';
import { useTranslation } from 'react-i18next';

interface Reply {
    content?: string;
    image?: string;
    [key: string]: string | undefined;
}

interface FieldErrors {
    [key: string]: string;
}

interface Props {
    classroomId: string;
    postId: string;
    parentId?: string;
}

export default function CreateReplyClient({ classroomId, postId, parentId }: Props) {
    const [reply, setReply] = useState<Reply>({});
    const imageRef = useRef<HTMLInputElement>(null);
    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const router = useRouter();
    const [previewImage, setPreviewImage] = useState<string | null>(null);
    const { t } = useTranslation();

    const setState = (value: string, field: string) => {
        setReply(prev => ({ ...prev, [field]: value }));
    };

    const validate = (): boolean => {
        if (!reply.content?.trim()) {
            setFieldErrors({ content: t("empty-error") });
            return false;
        }
        return true;
    };

    const handleAddReply = async (e: React.FormEvent) => {
        e.preventDefault();
        setMsg("");
        setFieldErrors({});

        if (validate()) {
            try {
                setLoading(true);
                const form = new FormData();

                Object.entries(reply).forEach(([key, value]) => {
                    if (value) form.append(key, value);
                });

                if (imageRef.current?.files?.[0]) {
                    form.append("file", imageRef.current.files[0]);
                }

                if (parentId) {
                    form.append("parentId", parentId);
                }

                const response = await authApis().post(
                    endpoints['forum-replies'](postId),
                    form,
                    { headers: { "Content-Type": "multipart/form-data" } }
                );
                console.log(response);

                // Simply redirect to the post page without query parameters
                router.push(`/classrooms/${classroomId}/forums/${postId}`);
            } catch (ex: any) {
                if (ex.response?.status === 400 && Array.isArray(ex.response.data)) {
                    const errors: FieldErrors = {};
                    ex.response.data.forEach((err: { field: string; message: string }) => {
                        errors[err.field] = err.message;
                    });
                    setFieldErrors(errors);
                } else {
                    setMsg(t("error-message"));
                }
            } finally {
                setLoading(false);
            }
        }
    };

    return (
        <div className="bg-white rounded-lg shadow-sm my-3 overflow-hidden">
            <div className="border-b px-6 py-4">
                <h3 className="text-xl font-semibold text-center">{t('reply')}</h3>
            </div>

            <div className="p-6">
                {msg && (
                    <div className="mb-4 p-4 bg-red-50 border-l-4 border-red-500 text-red-700">
                        {msg}
                    </div>
                )}

                <form onSubmit={handleAddReply} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t('content')}
                        </label>
                        <textarea
                            rows={5}
                            value={reply.content || ''}
                            onChange={(e) => setState(e.target.value, "content")}
                            placeholder={t('enter')}
                            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                ${fieldErrors.content ? 'border-red-500' : 'border-gray-300'}`}
                        />
                        {fieldErrors.content && (
                            <p className="mt-1 text-sm text-red-500">{fieldErrors.content}</p>
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
                                {t('send')}
                            </button>
                        )}
                    </div>
                </form>
            </div>
        </div>
    );
}