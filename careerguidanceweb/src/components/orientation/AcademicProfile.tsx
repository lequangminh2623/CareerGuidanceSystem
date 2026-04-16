'use client';

import { useEffect } from 'react';
import { useState } from 'react';
import {
    useGetStudentScoresQuery,
    useGetProfileQuery,
    useGetAttendanceStatisticsQuery,
} from '@/store/features/api/apiSlice';
import { useTranslation } from 'react-i18next';
import { FiBookOpen, FiClock, FiBriefcase, FiUsers, FiArrowRight } from 'react-icons/fi';

/* ───── Types ───── */
export interface SubjectAvg {
    name: string;
    score: number;
    icon: string;
}

export interface SurveyData {
    has_part_time_job: boolean;
    extracurricular_activities: boolean;
    self_study_hours: number;
    gender: string;
    absences: number;
}

interface Props {
    onSubmit: (scores: SubjectAvg[], survey: SurveyData) => void;
    isLoading: boolean;
}

/* ───── Component ───── */
export default function AcademicProfile({ onSubmit, isLoading }: Props) {
    const { t } = useTranslation();
    // RTK Query — 3 parallel queries (auto-cached, shared across pages)
    const { data: rawScores, isLoading: loadingScores, isError } = useGetStudentScoresQuery(undefined);
    const { data: profileData } = useGetProfileQuery();
    const { data: attendanceData } = useGetAttendanceStatisticsQuery();

    const [survey, setSurvey] = useState<SurveyData>({
        has_part_time_job: false,
        extracurricular_activities: false,
        self_study_hours: 10,
        gender: 'nam',
        absences: 0,
    });

    // Sync survey with profile + attendance data
    useEffect(() => {
        if (profileData || attendanceData) {
            setSurvey(prev => ({
                ...prev,
                gender: profileData?.gender === false ? 'nữ' : 'nam',
                absences: attendanceData?.absentCount || 0,
            }));
        }
    }, [profileData, attendanceData]);

    // Aggregate scores per subject
    const scores: SubjectAvg[] = (() => {
        if (!rawScores || !Array.isArray(rawScores)) return [];
        const subjectMap: Record<string, number[]> = {};
        for (const s of rawScores) {
            const name = s.subjectName || '';
            if (!subjectMap[name]) subjectMap[name] = [];
            const mid = s.midtermScore ?? 0;
            const fin = s.finalScore ?? 0;
            subjectMap[name].push(Math.round(((mid + fin) / 2) * 10) / 10);
        }
        return Object.entries(subjectMap).map(([name, vals]) => ({
            name,
            score: Math.round((vals.reduce((a, b) => a + b, 0) / vals.length) * 10) / 10,
            icon: '',
        }));
    })();

    const handleSubmit = () => {
        onSubmit(scores, survey);
    };

    const scoreColor = (v: number) => {
        if (v >= 8) return 'from-emerald-500 to-emerald-600';
        if (v >= 6.5) return 'from-blue-500 to-blue-600';
        if (v >= 5) return 'from-amber-500 to-amber-600';
        return 'from-red-500 to-red-600';
    };

    return (
        <div className="space-y-8 animate-in fade-in duration-700">
            {/* ══════ Header ══════ */}
            <div className="text-center space-y-4">
                <div>
                    <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">{t('orientation-steps-academic')}</h2>
                    <p className="text-gray-500 max-w-lg mx-auto text-sm mt-2">
                        {t('riasec-test-desc')}
                    </p>
                </div>

                <div className="flex justify-center gap-3">
                    <div className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-blue-50/80 text-blue-700 text-sm font-semibold border border-blue-100 shadow-sm transition-all hover:bg-blue-100">
                        <FiUsers className="w-4 h-4 text-blue-600" />
                        {t('gender')}: <span className="font-extrabold">{survey.gender === 'nữ' ? t('female') : t('male')}</span>
                    </div>
                    <div className="inline-flex items-center gap-1.5 px-4 py-2 rounded-full bg-rose-50/80 text-rose-700 text-sm font-semibold border border-rose-100 shadow-sm transition-all hover:bg-rose-100">
                        <FiClock className="w-4 h-4 text-rose-600" />
                        {t('absences')}: <span className="font-extrabold">{survey.absences}</span>
                    </div>
                </div>
            </div>

            {/* ══════ Score Display ══════ */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                    <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                        {t('gpa-subjects')}
                    </h3>
                </div>

                <div className="p-6">
                    {loadingScores ? (
                        <div className="flex justify-center py-12">
                            <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                        </div>
                    ) : isError ? (
                        <div className="text-center py-8">
                            <p className="text-red-500 font-medium">{t('load-error')}</p>
                        </div>
                    ) : scores.length === 0 ? (
                        <div className="text-center py-8 text-gray-400">
                            <p className="font-medium">{t('no-data-scores')}</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                            {scores.map((s) => (
                                <div key={s.name} className="group relative bg-white rounded-xl p-4 hover:shadow-sm transition-all duration-300 border border-gray-100">
                                    <div className="flex items-center gap-2 mb-3">
                                        <span className="text-xs font-bold text-gray-500 uppercase tracking-wider truncate">{s.name}</span>
                                    </div>
                                    <div className={`text-2xl font-extrabold bg-linear-to-r ${scoreColor(s.score)} bg-clip-text text-transparent`}>
                                        {s.score}
                                    </div>
                                    <div className="mt-2 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                                        <div
                                            className={`h-full rounded-full bg-linear-to-r ${scoreColor(s.score)} transition-all duration-700`}
                                            style={{ width: `${(s.score / 10) * 100}%` }}
                                        />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {/* ══════ Survey Form ══════ */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                    <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                        {t('additional-info')}
                    </h3>
                </div>

                <div className="p-6 space-y-6">
                    {/* Part-time job */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">
                            <FiBriefcase className="inline mr-1.5 text-indigo-500" />
                            {t('part-time-job-query')}
                        </label>
                        <div className="flex gap-3">
                            {[{ v: false, l: t('none') }, { v: true, l: t('confirm') }].map((o) => (
                                <button
                                    key={String(o.v)}
                                    type="button"
                                    onClick={() => setSurvey(p => ({ ...p, has_part_time_job: o.v }))}
                                    className={`flex-1 py-3 px-4 rounded-xl border-2 text-sm font-semibold transition-all ${survey.has_part_time_job === o.v
                                        ? 'border-indigo-500 bg-indigo-50 text-indigo-700 shadow-sm'
                                        : 'border-gray-200 text-gray-600 hover:border-indigo-200'
                                        }`}
                                >
                                    {o.l}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Extracurricular */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">
                            <FiUsers className="inline mr-1.5 text-indigo-500" />
                            {t('extracurricular-query')}
                        </label>
                        <div className="flex gap-3">
                            {[{ v: false, l: t('none') }, { v: true, l: t('confirm') }].map((o) => (
                                <button
                                    key={String(o.v)}
                                    type="button"
                                    onClick={() => setSurvey(p => ({ ...p, extracurricular_activities: o.v }))}
                                    className={`flex-1 py-3 px-4 rounded-xl border-2 text-sm font-semibold transition-all ${survey.extracurricular_activities === o.v
                                        ? 'border-indigo-500 bg-indigo-50 text-indigo-700 shadow-sm'
                                        : 'border-gray-200 text-gray-600 hover:border-indigo-200'
                                        }`}
                                >
                                    {o.l}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Self study hours */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">
                            <FiClock className="inline mr-1.5 text-indigo-500" />
                            {t('self-study-hours')}
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                min={0}
                                max={100}
                                value={survey.self_study_hours}
                                onChange={(e) => setSurvey(p => ({ ...p, self_study_hours: Math.max(0, parseInt(e.target.value) || 0) }))}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all text-gray-900 font-medium"
                                placeholder={t('self-study-placeholder')}
                            />
                            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm text-gray-400 font-medium">{t('hours-per-week')}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* ══════ Submit Button ══════ */}
            <button
                onClick={handleSubmit}
                disabled={isLoading}
                className="w-full py-4 px-6 bg-blue-600 text-white font-bold text-base rounded-2xl shadow-lg hover:bg-blue-700 transition-all duration-300 hover:-translate-y-0.5 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-3"
            >
                {isLoading ? (
                    <>
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        {t('analyzing')}
                    </>
                ) : (
                    <>
                        {t('get-advice')}
                        <FiArrowRight className="w-5 h-5" />
                    </>
                )}
            </button>
        </div>
    );
}
