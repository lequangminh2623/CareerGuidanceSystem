'use client';

import { useRef, useState } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { authApis, endpoints } from '@/lib/utils/api';
import MySpinner from '@/components/layout/MySpinner';
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";

interface Reply {
    id: string;
    content?: string;
    image?: string;
    createdDate?: string;
    updatedDate?: string;
    [key: string]: string | undefined;
}

interface FieldErrors {
    [key: string]: string;
}

interface Props {
    classroomId: string;
    postId: string;
    replyId: string;
}

export default function EditReplyClient({ classroomId, postId, replyId }: Props) {
    const [reply, setReply] = useState<Reply>({
        id: replyId,
        content: '',
        image: ''
    });
    const imageRef = useRef<HTMLInputElement>(null);
    const [loading, setLoading] = useState(false);
    const [msg, setMsg] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [previewImage, setPreviewImage] = useState<string | null>(reply.image || null);
    const router = useRouter();
    const { t } = useTranslation();

    const validate = (): boolean => {
        if (!reply.content?.trim()) {
            setFieldErrors({ content: 'Nội dung không được để trống' });
            return false;
        }
        return true;
    };

    const handleUpdateReply = async (e: React.FormEvent) => {
        e.preventDefault();
        setMsg("");
        setFieldErrors({});

        if (validate()) {
            try {
                setLoading(true);
                const form = new FormData();

                Object.entries(reply).forEach(([key, value]) => {
                    if (key !== 'createdDate' && key !== 'updatedDate' && value) {
                        form.append(key, value);
                    }
                });

                if (imageRef.current?.files?.[0]) {
                    form.append("file", imageRef.current.files[0]);
                }

                await authApis().patch(
                    endpoints['forum-reply-detail'](postId, reply.id),
                    form,
                    { headers: { "Content-Type": "multipart/form-data" } }
                );

                alert(t('update-reply-success'));
                router.back();
            } catch (ex: any) {
                if (ex.response?.status === 400 && Array.isArray(ex.response.data)) {
                    const errors: FieldErrors = {};
                    ex.response.data.forEach((err: { field: string; message: string }) => {
                        errors[err.field] = err.message;
                    });
                    setFieldErrors(errors);
                } else {
                    setMsg(t('update-reply-error'));
                }
            } finally {
                setLoading(false);
            }
        }
    };

    return (
        <div className="container mx-auto px-4 py-6 min-h-screen">
            <div className="bg-white rounded-lg shadow-sm overflow-hidden">
                <div className="border-b px-6 py-4">
                    <h3 className="text-xl font-semibold text-center">
                        {capitalizeFirstWord(`${t('edit')} ${t('reply')}`)}
                    </h3>
                </div>

                <div className="p-6">
                    {msg && (
                        <div className="mb-4 p-4 bg-red-50 border-l-4 border-red-500 text-red-700">
                            {msg}
                        </div>
                    )}

                    <form onSubmit={handleUpdateReply} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {t('content')}
                            </label>
                            <textarea
                                rows={5}
                                value={reply.content || ''}
                                onChange={(e) => setReply(prev => ({ ...prev, content: e.target.value }))}
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

                        {loading ? (
                            <MySpinner />
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <button
                                    type="button"
                                    onClick={() => router.back()}
                                    className="w-full px-4 py-2 bg-gray-500 text-white rounded-lg
                                             hover:bg-gray-600 transition-colors disabled:opacity-50"
                                >
                                    {t('cancel')}
                                </button>
                                <button
                                    type="submit"
                                    className="w-full px-4 py-2 bg-primary text-white rounded-lg
                                             hover:bg-primary-dark transition-colors disabled:opacity-50"
                                >
                                    {t('save')}
                                </button>
                            </div>
                        )}
                    </form>
                </div>
            </div>
        </div>
    );
}
