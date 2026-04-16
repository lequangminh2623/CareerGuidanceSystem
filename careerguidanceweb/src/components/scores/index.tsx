'use client';

import { useTranslation } from "react-i18next";
import { useGetStudentScoresQuery } from "@/store/features/api/apiSlice";
import { capitalizeFirstWord } from "@/lib/utils";
import MySpinner from "@/components/layout/MySpinner";
import SemesterTable from "./ScoreTable";
import { FiBookOpen, FiAlertCircle } from "react-icons/fi";

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
    const { i18n, t } = useTranslation();
    // RTK Query replaces useEffect + useState(loading) + useState(errorMsg)
    const { data: rawData, isLoading, isError, isFetching } = useGetStudentScoresQuery();

    // Data transformation (same logic as before)
    const scoresBySemester = (() => {
        if (!rawData || !Array.isArray(rawData)) return [];
        const grouped = rawData.reduce<SemesterGroup>((idx, score: StudentScoreResponseDTO) => {
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
                finalScore: score.finalScore !== undefined ? score.finalScore : null,
            });
            return idx;
        }, {});
        return Object.values(grouped);
    })();

    const loading = isLoading || isFetching;

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
            </div>

            {/* Error Message */}
            {isError && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3 animate-in fade-in slide-in-from-top-2">
                    <FiAlertCircle className="text-red-500 h-5 w-5 mt-0.5 shrink-0" />
                    <div>
                        <h4 className="text-sm font-semibold text-red-800">{t('error', 'Error')}</h4>
                        <p className="text-sm text-red-600 mt-1">{t('error-fetching-scores', 'Error loading scores.')}</p>
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
                            {t('no-scores-available', 'You do not have any recorded scores yet.')}
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
