'use client';

import { useEffect, useRef } from "react";
import { Chart, registerables } from "chart.js";
import { useGetStudentStatisticsQuery, useGetAttendanceStatisticsQuery } from "@/store/features/api/apiSlice";
import MySpinner from "../layout/MySpinner";
import { FiTrendingUp, FiBarChart2, FiPieChart, FiAlertCircle } from "react-icons/fi";
import { useTranslation } from "react-i18next";

Chart.register(...registerables);

const StudentStatistics = () => {
    const { t } = useTranslation();

    // ── RTK Query: 2 parallel queries (replaces Promise.all) ──
    const { data: statsData, isLoading: loadingScores, isError: scoresError } = useGetStudentStatisticsQuery();
    const { data: attendance, isLoading: loadingAttendance, isError: attendanceError } = useGetAttendanceStatisticsQuery();

    const semesterAverages = statsData?.semesterAverages ?? [];
    const yearAverages = statsData?.yearAverages ?? [];

    const loading = loadingScores || loadingAttendance;
    const error = scoresError || attendanceError ? t('error-loading-stats') : '';

    const lineChartRef = useRef<HTMLCanvasElement>(null);
    const barChartRef = useRef<HTMLCanvasElement>(null);
    const pieChartRef = useRef<HTMLCanvasElement>(null);
    const lineChartInstance = useRef<Chart | null>(null);
    const barChartInstance = useRef<Chart | null>(null);
    const pieChartInstance = useRef<Chart | null>(null);

    // Line chart — semester averages
    useEffect(() => {
        if (!lineChartRef.current || semesterAverages.length === 0) return;
        if (lineChartInstance.current) lineChartInstance.current.destroy();

        const ctx = lineChartRef.current.getContext("2d");
        if (!ctx) return;

        lineChartInstance.current = new Chart(ctx, {
            type: "line",
            data: {
                labels: semesterAverages.map(s => s.semesterLabel),
                datasets: [{
                    label: t('avg-semester'),
                    data: semesterAverages.map(s => s.avgScore),
                    borderColor: "rgb(59, 130, 246)",
                    backgroundColor: "rgba(59, 130, 246, 0.1)",
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: "rgb(59, 130, 246)",
                    pointBorderColor: "#fff",
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8,
                    borderWidth: 3,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: "top", labels: { usePointStyle: true, font: { size: 13, weight: "bold" } } },
                    tooltip: { backgroundColor: "rgba(15, 23, 42, 0.9)", titleFont: { size: 13 }, bodyFont: { size: 12 }, cornerRadius: 8, padding: 12 },
                },
                scales: {
                    y: { beginAtZero: true, max: 10, ticks: { stepSize: 1, font: { size: 12 } }, grid: { color: "rgba(0,0,0,0.06)" } },
                    x: { ticks: { font: { size: 11 }, maxRotation: 45 }, grid: { display: false } },
                },
            },
        });
        return () => { lineChartInstance.current?.destroy(); };
    }, [semesterAverages, t]);

    // Bar chart — yearly averages
    useEffect(() => {
        if (!barChartRef.current || yearAverages.length === 0) return;
        if (barChartInstance.current) barChartInstance.current.destroy();

        const ctx = barChartRef.current.getContext("2d");
        if (!ctx) return;

        const colors = ["rgba(16,185,129,0.8)", "rgba(59,130,246,0.8)", "rgba(168,85,247,0.8)", "rgba(245,158,11,0.8)", "rgba(239,68,68,0.8)"];

        barChartInstance.current = new Chart(ctx, {
            type: "bar",
            data: {
                labels: yearAverages.map(y => y.yearName),
                datasets: [{
                    label: t('avg-year'),
                    data: yearAverages.map(y => y.avgScore),
                    backgroundColor: yearAverages.map((_, i) => colors[i % colors.length]),
                    borderColor: yearAverages.map((_, i) => colors[i % colors.length].replace("0.8", "1")),
                    borderWidth: 2, borderRadius: 8, borderSkipped: false,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                plugins: {
                    legend: { display: true, position: "top", labels: { usePointStyle: true, font: { size: 13, weight: "bold" } } },
                    tooltip: { backgroundColor: "rgba(15, 23, 42, 0.9)", cornerRadius: 8, padding: 12 },
                },
                scales: {
                    y: { beginAtZero: true, max: 10, ticks: { stepSize: 1, font: { size: 12 } }, grid: { color: "rgba(0,0,0,0.06)" } },
                    x: { ticks: { font: { size: 12, weight: "bold" } }, grid: { display: false } },
                },
            },
        });
        return () => { barChartInstance.current?.destroy(); };
    }, [yearAverages, t]);

    // Doughnut chart — attendance
    useEffect(() => {
        if (!pieChartRef.current || !attendance) return;
        if (pieChartInstance.current) pieChartInstance.current.destroy();

        const ctx = pieChartRef.current.getContext("2d");
        if (!ctx) return;

        const present = attendance.presentCount ?? 0;
        const late = attendance.lateCount ?? 0;
        const absent = attendance.absentCount ?? 0;
        const total = present + late + absent;
        if (total === 0) return;

        pieChartInstance.current = new Chart(ctx, {
            type: "doughnut",
            data: {
                labels: [t('present'), t('late'), t('absent')],
                datasets: [{
                    data: [present, late, absent],
                    backgroundColor: ["rgba(16,185,129,0.85)", "rgba(245,158,11,0.85)", "rgba(239,68,68,0.85)"],
                    borderColor: ["rgb(16,185,129)", "rgb(245,158,11)", "rgb(239,68,68)"],
                    borderWidth: 2, hoverOffset: 8,
                }],
            },
            options: {
                responsive: true, maintainAspectRatio: false, cutout: "60%",
                plugins: {
                    legend: { position: "bottom", labels: { usePointStyle: true, padding: 20, font: { size: 13, weight: "bold" } } },
                    tooltip: {
                        backgroundColor: "rgba(15, 23, 42, 0.9)", cornerRadius: 8, padding: 12,
                        callbacks: {
                            label: (context) => {
                                const value = context.raw as number;
                                const pct = ((value / total) * 100).toFixed(1);
                                return ` ${context.label}: ${value} (${pct}%)`;
                            },
                        },
                    },
                },
            },
        });
        return () => { pieChartInstance.current?.destroy(); };
    }, [attendance, t]);

    if (loading) {
        return <div className="flex justify-center items-center py-20"><MySpinner /></div>;
    }

    if (error) {
        return (
            <div className="max-w-2xl mx-auto mt-8 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
                <FiAlertCircle className="text-red-500 h-5 w-5 mt-0.5 shrink-0" />
                <p className="text-sm text-red-600">{error}</p>
            </div>
        );
    }

    const present = attendance?.presentCount ?? 0;
    const late = attendance?.lateCount ?? 0;
    const absent = attendance?.absentCount ?? 0;
    const totalAttendance = present + late + absent;

    return (
        <div className="space-y-8">
            {/* Summary Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl p-5 text-white shadow-lg shadow-blue-500/20">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="bg-white/20 p-2 rounded-lg"><FiTrendingUp className="h-5 w-5" /></div>
                        <span className="text-sm font-medium text-blue-100">{t('avg-semester-recent')}</span>
                    </div>
                    <p className="text-3xl font-bold">
                        {semesterAverages.length > 0 ? semesterAverages[semesterAverages.length - 1].avgScore.toFixed(2) : "—"}
                    </p>
                </div>
                <div className="bg-linear-to-br from-emerald-500 to-emerald-600 rounded-2xl p-5 text-white shadow-lg shadow-emerald-500/20">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="bg-white/20 p-2 rounded-lg"><FiBarChart2 className="h-5 w-5" /></div>
                        <span className="text-sm font-medium text-emerald-100">{t('avg-year-recent')}</span>
                    </div>
                    <p className="text-3xl font-bold">
                        {yearAverages.length > 0 ? yearAverages[yearAverages.length - 1].avgScore.toFixed(2) : "—"}
                    </p>
                </div>
                <div className="bg-linear-to-br from-purple-500 to-purple-600 rounded-2xl p-5 text-white shadow-lg shadow-purple-500/20">
                    <div className="flex items-center gap-3 mb-2">
                        <div className="bg-white/20 p-2 rounded-lg"><FiPieChart className="h-5 w-5" /></div>
                        <span className="text-sm font-medium text-purple-100">{t('total-attendance')}</span>
                    </div>
                    <p className="text-3xl font-bold">{totalAttendance}</p>
                </div>
            </div>

            {/* Line + Bar charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <FiTrendingUp className="text-blue-500" />{t('avg-semester-over-time')}
                    </h3>
                    <div className="h-72">
                        {semesterAverages.length > 0
                            ? <canvas ref={lineChartRef} />
                            : <div className="flex items-center justify-center h-full text-gray-400 text-sm">{t('no-data-scores')}</div>}
                    </div>
                </div>
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <FiBarChart2 className="text-emerald-500" />{t('avg-year-over-time')}
                    </h3>
                    <div className="h-72">
                        {yearAverages.length > 0
                            ? <canvas ref={barChartRef} />
                            : <div className="flex items-center justify-center h-full text-gray-400 text-sm">{t('no-data-scores')}</div>}
                    </div>
                </div>
            </div>

            {/* Pie + summary */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                        <FiPieChart className="text-purple-500" />{t('attendance-rate')}
                    </h3>
                    <div className="h-72">
                        {totalAttendance > 0
                            ? <canvas ref={pieChartRef} />
                            : <div className="flex items-center justify-center h-full text-gray-400 text-sm">{t('no-data-attendance')}</div>}
                    </div>
                </div>

                {attendance && totalAttendance > 0 && (
                    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
                        <h3 className="text-lg font-bold text-gray-900 mb-4">{t('attendance-detail')}</h3>
                        <div className="space-y-4 mt-6">
                            {[
                                { color: "emerald", label: t('present'), count: present },
                                { color: "amber", label: t('late'), count: late },
                                { color: "red", label: t('absent'), count: absent },
                            ].map(({ color, label, count }) => (
                                <div key={label} className={`flex items-center justify-between p-4 bg-${color}-50 rounded-xl`}>
                                    <div className="flex items-center gap-3">
                                        <div className={`w-3 h-3 rounded-full bg-${color}-500`} />
                                        <span className="font-medium text-gray-700">{label}</span>
                                    </div>
                                    <div className="text-right">
                                        <span className={`text-lg font-bold text-${color}-600`}>{count}</span>
                                        <span className="text-sm text-gray-400 ml-2">
                                            ({((count / totalAttendance) * 100).toFixed(1)}%)
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StudentStatistics;
