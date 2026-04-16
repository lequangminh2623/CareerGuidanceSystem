'use client';

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAppSelector } from "@/store/hooks";
import { useGetTranscriptListQuery } from "@/store/features/api/apiSlice";
import MySpinner from "@/components/layout/MySpinner";
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";
import { FiUser, FiCalendar, FiBookOpen, FiAlertCircle } from "react-icons/fi";
import { FaChalkboardTeacher } from "react-icons/fa";

export default function TranscriptListClient() {
    const [page, setPage] = useState(1);
    const searchParams = useSearchParams();
    const router = useRouter();
    const user = useAppSelector((state) => state.auth.user);
    const { t } = useTranslation();

    const kw = searchParams.get('kw') ?? undefined;
    const sortBy = searchParams.get('sortBy') ?? undefined;

    // ── RTK Query: replaces useCallback + useEffect + useState(loading/error/transcripts) ──
    const { data, isLoading, isFetching, isError } = useGetTranscriptListQuery({ page, kw, sortBy });

    const transcripts = data?.transcripts?.content ?? [];
    const totalPages = data?.transcripts?.totalPages ?? 1;

    return (
        <div className="container mx-auto px-4 py-8 max-w-7xl min-h-screen">
            {/* Header */}
            <div className="mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight flex items-center gap-3">
                        <FaChalkboardTeacher className="text-blue-600 h-8 w-8" />
                        {capitalizeFirstWord(t('list-transcripts'))}
                    </h1>
                    <p className="mt-2 text-sm text-gray-500">{t('transcripts-subtitle')}</p>
                </div>
            </div>

            {/* Error */}
            {isError && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3 animate-in fade-in slide-in-from-top-2">
                    <FiAlertCircle className="text-red-500 h-5 w-5 mt-0.5 shrink-0" />
                    <div>
                        <h4 className="text-sm font-semibold text-red-800">{t('error')}</h4>
                        <p className="text-sm text-red-600 mt-1">{t('load-error')}</p>
                    </div>
                </div>
            )}

            {/* Empty state */}
            {!isLoading && !isError && transcripts.length === 0 && (
                <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-12 text-center flex flex-col items-center justify-center max-w-2xl mx-auto animate-in fade-in">
                    <div className="bg-blue-50 p-4 rounded-full mb-4">
                        <FaChalkboardTeacher className="h-8 w-8 text-blue-500" />
                    </div>
                    <h3 className="text-lg font-bold text-gray-900 mb-2">{capitalizeFirstWord(t("none-transcripts"))}</h3>
                    <p className="text-gray-500 text-sm max-w-sm">{t('no-transcripts-available')}</p>
                </div>
            )}

            {/* Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 relative">
                {transcripts.map((tItem, idx) => (
                    <div
                        key={tItem.id || idx}
                        className="group bg-white rounded-2xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.07)] border border-gray-100 hover:shadow-2xl hover:shadow-blue-500/10 hover:-translate-y-1 transition-all duration-300 flex flex-col"
                    >
                        <div className="h-2 w-full bg-linear-to-r from-blue-500 to-indigo-500 rounded-t-2xl" />

                        <div className="p-6 flex flex-col flex-1">
                            {/* Subject & class */}
                            <div className="mb-4">
                                <div className="flex items-start justify-between gap-2 mb-1">
                                    <span className="text-[10px] font-black uppercase tracking-widest text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                                        {tItem.gradeName}
                                    </span>
                                    <span className="text-[10px] font-bold bg-gray-100 text-gray-600 px-2 py-0.5 rounded">
                                        {tItem.semesterName}
                                    </span>
                                </div>
                                <h3 className="text-xl font-extrabold text-gray-900 line-clamp-1 group-hover:text-blue-700 transition-colors">
                                    {tItem.subjectName}
                                </h3>
                                <p className="text-sm font-semibold text-gray-500 mt-0.5 flex items-center gap-1">
                                    {t('class')}: <span className="text-gray-700">{tItem.classroomName}</span>
                                </p>
                            </div>

                            {/* Teacher */}
                            <div className="mt-auto pt-4 border-t border-dashed border-gray-100">
                                <div className="flex items-center gap-3">
                                    <div className="h-9 w-9 rounded-full bg-linear-to-br from-gray-100 to-gray-200 flex items-center justify-center text-gray-500 border border-white shadow-sm">
                                        <FiUser className="h-4 w-4" />
                                    </div>
                                    <div className="overflow-hidden">
                                        <p className="text-[11px] text-gray-400 font-bold uppercase tracking-tighter">{t('teacher-in-charge')}</p>
                                        <p className="text-sm font-bold text-gray-700 truncate">{tItem.teacherName}</p>
                                    </div>
                                </div>
                            </div>

                            {/* Year */}
                            <div className="mt-3 flex items-center gap-1.5 text-[12px] font-medium text-gray-500 bg-gray-50/80 w-fit px-3 py-1 rounded-full">
                                <FiCalendar className="h-3 w-3" />
                                <span>{t('year')}: {tItem.yearName}</span>
                            </div>

                            {/* Actions */}
                            <div className="grid grid-cols-2 gap-3 mt-6">
                                {user?.role === "Teacher" && (
                                    <button
                                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                                        onClick={() => router.push(`/transcripts/${tItem.id}` as any)}
                                        className="flex justify-center items-center gap-2 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 shadow-lg shadow-gray-200 hover:shadow-blue-200 transition-all text-xs"
                                    >
                                        <FiBookOpen className="h-4 w-4" />
                                        {t('grades-manage')}
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                ))}

                {/* Loading overlay when fetching new pages */}
                {(isLoading || isFetching) && (
                    <div className="absolute inset-0 bg-white/60 backdrop-blur-sm z-10 flex justify-center items-center rounded-2xl">
                        <MySpinner />
                    </div>
                )}
            </div>

            {/* Pagination */}
            {!isError && totalPages > 1 && (
                <div className="flex justify-center mt-10 gap-2">
                    {[...Array(totalPages)].map((_, i) => (
                        <button
                            key={i + 1}
                            onClick={() => setPage(i + 1)}
                            className={`min-w-[40px] h-10 px-3 rounded-xl font-medium text-sm transition-all focus:outline-none focus:ring-2 focus:ring-blue-500/20 ${
                                page === i + 1
                                    ? 'bg-blue-600 text-white shadow-md shadow-blue-200'
                                    : 'bg-white text-gray-600 hover:bg-blue-50 border border-gray-200 hover:border-blue-200 hover:text-blue-600'
                            }`}
                        >
                            {i + 1}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}
