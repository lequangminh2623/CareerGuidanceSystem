'use client';

import { useEffect, useRef, useState } from "react";
import { Chart, registerables } from "chart.js";
import {
    useGetTeacherSectionsQuery,
    useGetSubjectsQuery,
    useGetTeacherGradesQuery,
} from "@/store/features/api/apiSlice";
import MySpinner from "../layout/MySpinner";
import { FiBarChart2, FiTrendingUp, FiFilter, FiAlertCircle } from "react-icons/fi";
import { useTranslation } from "react-i18next";

Chart.register(...registerables);

const GRADE_COLORS: Record<string, { border: string; bg: string }> = {
    "Grade 10": { border: "rgb(59, 130, 246)", bg: "rgba(59, 130, 246, 0.1)" },
    "Grade 11": { border: "rgb(16, 185, 129)", bg: "rgba(16, 185, 129, 0.1)" },
    "Grade 12": { border: "rgb(168, 85, 247)", bg: "rgba(168, 85, 247, 0.1)" },
};

const TeacherStatistics = () => {
    const { t } = useTranslation();
    const [selectedSubject, setSelectedSubject] = useState<string>("");

    // ── RTK Query: 3 parallel queries ──
    const { data: sectionAvgs = [], isLoading: loadingSections, isError: sectionsError } = useGetTeacherSectionsQuery();
    const { data: subjects = [], isLoading: loadingSubjects } = useGetSubjectsQuery();

    // Grades query re-fetches automatically when selectedSubject changes
    const {
        data: gradeStats = [],
        isLoading: gradeLoading,
        isError: gradesError,
        isFetching: gradeFetching,
    } = useGetTeacherGradesQuery(selectedSubject || undefined);

    const loading = loadingSections || loadingSubjects;
    const error = sectionsError || gradesError ? t('error-loading-stats') : '';

    const barChartRef = useRef<HTMLCanvasElement>(null);
    const barChartInstance = useRef<Chart | null>(null);
    const gradeChartRefs = useRef<Record<string, HTMLCanvasElement | null>>({});
    const gradeChartInstances = useRef<Record<string, Chart>>({});

    // Bar chart — section averages
    useEffect(() => {
        if (!barChartRef.current || sectionAvgs.length === 0) return;
        if (barChartInstance.current) barChartInstance.current.destroy();

        const ctx = barChartRef.current.getContext("2d");
        if (!ctx) return;

        const colors = sectionAvgs.map((_, i) => {
            const hue = (i * 360 / sectionAvgs.length + 200) % 360;
            return `hsla(${hue}, 70%, 55%, 0.85)`;
        });

        barChartInstance.current = new Chart(ctx, {
            type: "bar",
            data: {
                labels: sectionAvgs.map(s => s.sectionLabel),
                datasets: [{
                    label: t('avg-section'),
                    data: sectionAvgs.map(s => s.avgScore),
                    backgroundColor: colors,
                    borderColor: colors.map(c => c.replace("0.85", "1")),
                    borderWidth: 2, borderRadius: 8, borderSkipped: false,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { backgroundColor: "rgba(15,23,42,0.9)", cornerRadius: 8, padding: 12, titleFont: { size: 13 }, bodyFont: { size: 12 } },
                },
                scales: {
                    y: { beginAtZero: true, max: 10, ticks: { stepSize: 1, font: { size: 12 } }, grid: { color: "rgba(0,0,0,0.06)" } },
                    x: { ticks: { font: { size: 10 }, maxRotation: 45, minRotation: 0 }, grid: { display: false } },
                },
            },
        });
        return () => { barChartInstance.current?.destroy(); };
    }, [sectionAvgs, t]);

    // Grade line charts — re-draw when gradeStats changes
    useEffect(() => {
        Object.values(gradeChartInstances.current).forEach(c => c.destroy());
        gradeChartInstances.current = {};

        if (gradeStats.length === 0) return;

        gradeStats.forEach(grade => {
            const canvas = gradeChartRefs.current[grade.gradeName];
            if (!canvas || grade.semesterAverages.length === 0) return;

            const ctx = canvas.getContext("2d");
            if (!ctx) return;

            const colorSet = GRADE_COLORS[grade.gradeName] || { border: "rgb(107,114,128)", bg: "rgba(107,114,128,0.1)" };

            gradeChartInstances.current[grade.gradeName] = new Chart(ctx, {
                type: "line",
                data: {
                    labels: grade.semesterAverages.map(s => s.semesterLabel),
                    datasets: [{
                        label: `${t('avg-grade')} ${grade.gradeName}`,
                        data: grade.semesterAverages.map(s => s.avgScore),
                        borderColor: colorSet.border,
                        backgroundColor: colorSet.bg,
                        fill: true, tension: 0.4,
                        pointBackgroundColor: colorSet.border,
                        pointBorderColor: "#fff",
                        pointBorderWidth: 2, pointRadius: 6, pointHoverRadius: 8, borderWidth: 3,
                    }],
                },
                options: {
                    responsive: true, maintainAspectRatio: false,
                    plugins: {
                        legend: { display: true, position: "top", labels: { usePointStyle: true, font: { size: 13, weight: "bold" } } },
                        tooltip: { backgroundColor: "rgba(15,23,42,0.9)", cornerRadius: 8, padding: 12 },
                    },
                    scales: {
                        y: { beginAtZero: true, max: 10, ticks: { stepSize: 1, font: { size: 12 } }, grid: { color: "rgba(0,0,0,0.06)" } },
                        x: { ticks: { font: { size: 11 }, maxRotation: 45 }, grid: { display: false } },
                    },
                },
            });
        });

        return () => {
            Object.values(gradeChartInstances.current).forEach(c => c.destroy());
            gradeChartInstances.current = {};
        };
    }, [gradeStats, t]);

    if (loading) return <div className="flex justify-center items-center py-20"><MySpinner /></div>;

    if (error) {
        return (
            <div className="max-w-2xl mx-auto mt-8 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
                <FiAlertCircle className="text-red-500 h-5 w-5 mt-0.5 shrink-0" />
                <p className="text-sm text-red-600">{error}</p>
            </div>
        );
    }

    return (
        <div className="space-y-8">
            {/* Section bar chart */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                    <FiBarChart2 className="text-blue-500" />
                    {t('avg-section-recent')}
                </h3>
                <div className="h-80">
                    {sectionAvgs.length > 0
                        ? <canvas ref={barChartRef} />
                        : <div className="flex items-center justify-center h-full text-gray-400 text-sm">{t('no-data-scores')}</div>}
                </div>
            </div>

            {/* Grade trend charts with subject filter */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
                    <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                        <FiTrendingUp className="text-emerald-500" />
                        {t('avg-grade-semesters')}
                    </h3>
                    <div className="flex items-center gap-2">
                        <FiFilter className="text-gray-400 h-4 w-4" />
                        <select
                            value={selectedSubject}
                            onChange={(e) => setSelectedSubject(e.target.value)}
                            className="px-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium text-gray-700 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all min-w-[200px]"
                        >
                            <option value="">{t('all-subjects')}</option>
                            {subjects.map(s => (
                                <option key={s.id} value={s.name}>{s.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {gradeLoading || gradeFetching ? (
                    <div className="flex justify-center items-center py-16"><MySpinner /></div>
                ) : gradeStats.length > 0 ? (
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        {gradeStats.map(grade => (
                            <div key={grade.gradeName} className="bg-gray-50 rounded-xl p-4">
                                <h4 className="text-sm font-bold text-gray-600 mb-3 text-center uppercase tracking-wide">
                                    {grade.gradeName}
                                </h4>
                                <div className="h-56">
                                    {grade.semesterAverages.length > 0 ? (
                                        <canvas
                                            ref={el => { gradeChartRefs.current[grade.gradeName] = el; }}
                                        />
                                    ) : (
                                        <div className="flex items-center justify-center h-full text-gray-400 text-sm">{t('no-data')}</div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="flex items-center justify-center py-16 text-gray-400 text-sm">
                        {t('no-data-grade-scores')}
                    </div>
                )}
            </div>
        </div>
    );
};

export default TeacherStatistics;
