'use client';

import { useEffect, useState } from "react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useTranslation } from "react-i18next";
import axios from "axios";
import { authApis, endpoints } from "@/lib/utils/api";
import { capitalizeFirstWord } from "@/lib/utils";
import MySpinner from "@/components/layout/MySpinner";
import SemesterTable from "./ScoreTable";
import { FiSearch, FiBookOpen, FiAlertCircle } from "react-icons/fi";

interface StudentScoreResponseDTO {
    id: string;
    midtermScore: number | null;
    finalScore: number | null;
    extraScores: number[];
    subjectName: string;
    classroomName: string;
    semesterName: string;
    yearName: string;
}

export interface Subject {
    id: string;
    classCode: string;
    name: string;
    extraScore: number[];
    midTermScore: number | null;
    finalScore: number | null;
}

interface SemesterGroup {
    [key: string]: {
        semesterTitle: string;
        classroomName: string;
        subjects: Subject[];
    };
}

const ScoresClient = () => {
    const [scoresBySemester, setScoresBySemester] = useState<Array<{
        semesterTitle: string;
        classroomName: string;
        subjects: Subject[];
    }>>([]);
    const [loading, setLoading] = useState(false);
    const [errorMsg, setErrorMsg] = useState("");
    const [searchTerm, setSearchTerm] = useState("");

    const searchParams = useSearchParams();
    const router = useRouter();
    const pathname = usePathname();
    const { i18n, t } = useTranslation();

    const loadScores = async () => {
        try {
            setLoading(true);
            setErrorMsg("");
            let url = endpoints['student-scores'];
            const kw = searchParams.get('kw');

            if (kw) {
                url = `${url}?kw=${kw}`;
                if (searchTerm !== kw) setSearchTerm(kw);
            }

            const res = await authApis().get(url);
            const data: StudentScoreResponseDTO[] = res.data;
            console.info(data)


            if (!data || !Array.isArray(data)) {
                setScoresBySemester([]);
                return;
            }

            const grouped = data.reduce<SemesterGroup>((idx, score) => {
                const sName = score.semesterName || t("unknown-semester");
                const yName = score.yearName || t("unknown-year");
                const cName = score.classroomName || t("unknown-classroom");
                const groupKey = `${cName} - ${sName} - ${yName}`;

                if (!idx[groupKey]) {
                    idx[groupKey] = {
                        semesterTitle: `${sName} (${yName})`,
                        classroomName: cName,
                        subjects: [],
                    };
                }

                idx[groupKey].subjects.push({
                    id: score.id || Math.random().toString(),
                    classCode: score.classroomName || "-",
                    name: score.subjectName || "-",
                    extraScore: score.extraScores || [],
                    midTermScore: score.midtermScore !== undefined ? score.midtermScore : null,
                    finalScore: score.finalScore !== undefined ? score.finalScore : null
                });
                return idx;
            }, {});

            setScoresBySemester(Object.values(grouped));
        } catch (ex: unknown) {
            console.error(ex);
            if (axios.isAxiosError(ex)) {
                const data = ex.response?.data as { message?: string } | undefined;
                setErrorMsg(data?.message || t('error-fetching-scores', 'Error loading scores.'));
            } else {
                setErrorMsg(t('error-fetching-scores', 'Error loading scores.'));
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadScores();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchParams, i18n.language]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        const currentParams = new URLSearchParams(Array.from(searchParams.entries()));

        if (searchTerm.trim() === "") {
            currentParams.delete("kw");
        } else {
            currentParams.set("kw", searchTerm.trim());
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        router.push(`${pathname}?${currentParams.toString()}` as any);
    };

    return (
        <div className="container mx-auto px-4 py-8 max-w-7xl min-h-screen">
            {/* Header Section */}
            <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight flex items-center gap-3">
                        <FiBookOpen className="text-blue-600 h-8 w-8" />
                        {t('my-scores')}
                    </h1>
                    <p className="mt-2 text-sm text-gray-500">
                        {t('scores-subtitle')}
                    </p>
                </div>

                {/* Search Form with Validation */}
                <form onSubmit={handleSearch} className="relative w-full md:w-96 group">
                    <div className="relative flex items-center">
                        <FiSearch className="absolute left-3 text-gray-400 h-5 w-5 group-focus-within:text-blue-500 transition-colors" />
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            placeholder={t('search-course', 'Search by course name...')}
                            className="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl shadow-sm focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all placeholder:text-gray-400 text-sm font-medium text-gray-900"
                        />
                        <button
                            type="submit"
                            className="absolute right-2 px-3 py-1.5 bg-blue-50 text-blue-600 font-medium text-xs rounded-lg hover:bg-blue-100 transition-colors"
                        >
                            {t('search', 'Search')}
                        </button>
                    </div>
                </form>
            </div>

            {/* Error Message from Backend validation */}
            {errorMsg && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3 animate-in fade-in slide-in-from-top-2">
                    <FiAlertCircle className="text-red-500 h-5 w-5 mt-0.5 shrink-0" />
                    <div>
                        <h4 className="text-sm font-semibold text-red-800">{t('error', 'Error')}</h4>
                        <p className="text-sm text-red-600 mt-1">{errorMsg}</p>
                    </div>
                </div>
            )}

            {/* Main Content */}
            <div className="relative">
                {loading && scoresBySemester.length === 0 ? (
                    <div className="flex justify-center items-center py-20">
                        <MySpinner />
                    </div>
                ) : scoresBySemester.length > 0 ? (
                    <div className="space-y-8 animate-in fade-in duration-500">
                        {scoresBySemester.map((group, idx) => (
                            <SemesterTable
                                key={idx}
                                semesterTitle={group.semesterTitle}
                                classroomName={group.classroomName}
                                subjects={group.subjects}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-12 text-center flex flex-col items-center justify-center max-w-2xl mx-auto">
                        <div className="bg-blue-50 p-4 rounded-full mb-4">
                            <FiBookOpen className="h-8 w-8 text-blue-500" />
                        </div>
                        <h3 className="text-lg font-bold text-gray-900 mb-2">
                            {capitalizeFirstWord(`${t('no-results', 'No scores found')}`)}
                        </h3>
                        <p className="text-gray-500 text-sm max-w-sm">
                            {searchTerm
                                ? t('no-search-results', 'Could not find any scores matching your search criteria.')
                                : t('no-scores-available', 'You do not have any recorded scores yet.')}
                        </p>
                    </div>
                )}

                {/* Overlay spinner when searching/loading but already have data */}
                {loading && scoresBySemester.length > 0 && (
                    <div className="absolute inset-0 bg-white/50 backdrop-blur-[2px] z-10 flex justify-center items-center rounded-2xl">
                        <MySpinner />
                    </div>
                )}
            </div>
        </div>
    );
};

export default ScoresClient;
