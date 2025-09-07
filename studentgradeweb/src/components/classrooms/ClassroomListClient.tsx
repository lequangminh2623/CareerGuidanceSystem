'use client';

import { useEffect, useState, useCallback, useContext } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { MyUserContext } from "@/lib/contexts/userContext";
import { authApis, endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";

interface Classroom {
    id: string;
    name: string;
    gradeStatus: string;
    course?: {
        name: string;
    };
    semester?: {
        academicYear: {
            year: string;
        };
        semesterType: string;
    };
    teacher?: {
        lastName: string;
        firstName: string;
    };
}

interface PageResponse {
    content: Classroom[];
    totalPages: number;
}

export default function ClassroomListClient() {
    const [classrooms, setClassrooms] = useState<Classroom[]>([]);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string>('');
    const searchParams = useSearchParams();
    const router = useRouter();
    const user = useContext(MyUserContext);
    const { t } = useTranslation();

    const loadClassrooms = useCallback(async () => {
        try {
            setLoading(true);
            setError('');
            let url = `${endpoints['classrooms']}?page=${page}`;

            const kw = searchParams.get('kw');
            if (kw) url += `&kw=${kw}`;

            const sortBy = searchParams.get('sortBy');
            if (sortBy) url += `&sortBy=${sortBy}`;

            const res = await authApis().get<PageResponse>(url);

            // Ensure we have valid data structure
            if (res.data && res.data.content && Array.isArray(res.data.content)) {
                setClassrooms(res.data.content);
                setTotalPages(res.data.totalPages || 1);
            } else {
                setClassrooms([]);
                setTotalPages(1);
            }
        } catch (ex: any) {
            console.error("Failed to load classrooms:", ex);
            setClassrooms([]);
            setError(ex.response?.status === 403 ?
                (t('access-denied') || 'Bạn không có quyền truy cập') :
                (t('load-error') || 'Có lỗi xảy ra khi tải dữ liệu')
            );
        } finally {
            setLoading(false);
        }
    }, [page, searchParams, t]);

    useEffect(() => {
        loadClassrooms();
    }, [loadClassrooms]);

    return (
        <div className="container mx-auto px-4 py-6 min-h-screen">
            <h2 className="text-2xl font-bold mb-6">{t('list-classrooms')}</h2>

            {error && (
                <div className="bg-red-50 border-l-4 border-red-400 p-4 text-red-700 mb-6">
                    {error}
                </div>
            )}

            {!loading && !error && classrooms.length === 0 && (
                <div className="bg-blue-50 border-l-4 border-blue-400 p-4 text-blue-700">
                    {capitalizeFirstWord(`${t("none")} ${t("classrooms")}`)}
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array.isArray(classrooms) && classrooms.map(c => (
                    <div key={c.id} className="bg-gray-50 rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
                        <h3 className="text-xl font-semibold mb-4">{c.name}</h3>

                        <div className="space-y-2 text-gray-600 mb-6">
                            <p><strong>{t('grades-status')}:</strong> {c.gradeStatus}</p>
                            <p><strong>{t('course')}:</strong> {c.course?.name}</p>
                            <p><strong>{t('semester')}:</strong> {c.semester?.academicYear.year} - {c.semester?.semesterType}</p>
                            <p><strong>{t('teacher')}:</strong> {c.teacher?.lastName} {c.teacher?.firstName}</p>
                        </div>

                        <div className="space-x-3">
                            <button
                                onClick={() => router.push(`/classrooms/${c.id}/forums`)}
                                className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                            >
                                {t('forum')}
                            </button>

                            {user?.role === "ROLE_TEACHER" && (
                                <button
                                    onClick={() => router.push(`/classrooms/${c.id}`)}
                                    className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary-dark transition-colors"
                                >
                                    {t('grades-manage')}
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            {!error && totalPages > 1 && (
                <div className="flex justify-center mt-6 space-x-2">
                    {[...Array(totalPages)].map((_, i) => (
                        <button
                            key={i + 1}
                            onClick={() => setPage(i + 1)}
                            className={`px-3 py-1 rounded ${page === i + 1
                                ? 'bg-primary text-white'
                                : 'bg-white text-gray-700 hover:bg-gray-100'
                                }`}
                        >
                            {i + 1}
                        </button>
                    ))}
                </div>
            )}

            {loading && <MySpinner />}
        </div>
    );
}
