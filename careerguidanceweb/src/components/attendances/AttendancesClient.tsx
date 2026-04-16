"use client";

import { useState } from "react";
import {
    useGetAttendanceClassroomsQuery,
    useGetAttendancesByClassroomQuery,
} from "@/store/features/api/apiSlice";
import { useTranslation } from "react-i18next";
import { FiBookOpen, FiClock, FiCheckCircle, FiXCircle, FiAlertCircle, FiSearch, FiCalendar } from "react-icons/fi";

const getStatusConfig = (status: string, t: (k: string) => string) => {
    switch (status) {
        case "Present":
            return { label: t("present"), icon: <FiCheckCircle className="w-4 h-4" />, class: "bg-emerald-50 text-emerald-700 border-emerald-200" };
        case "Absent":
            return { label: t("absent"), icon: <FiXCircle className="w-4 h-4" />, class: "bg-red-50 text-red-700 border-red-200" };
        case "Late":
            return { label: t("late"), icon: <FiClock className="w-4 h-4" />, class: "bg-amber-50 text-amber-700 border-amber-200" };
        default:
            return { label: status, icon: <FiAlertCircle className="w-4 h-4" />, class: "bg-gray-50 text-gray-700 border-gray-200" };
    }
};

export default function AttendancesClient() {
    const { t, i18n } = useTranslation();
    const [selectedClassroomId, setSelectedClassroomId] = useState<string>("");

    // ── RTK Query ──
    const {
        data: classrooms = [],
        isLoading: loadingClassrooms,
        isError: classroomsError,
    } = useGetAttendanceClassroomsQuery();

    const {
        data: attendances = [],
        isLoading: loadingAttendances,
        isError: attendancesError,
    } = useGetAttendancesByClassroomQuery(selectedClassroomId, {
        // Only fetch when a classroom is selected
        skip: !selectedClassroomId,
    });

    const error = classroomsError
        ? t("cannot-load-classrooms")
        : attendancesError
        ? t("loading-attendances-error")
        : "";

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl min-h-screen">
            {/* Header */}
            <div className="mb-8 text-center md:text-left flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900 tracking-tight mb-1">
                        {t("attendance-search")}
                    </h1>
                    <p className="text-sm text-gray-500 font-medium">
                        {selectedClassroomId
                            ? `${t("attendance-history-for")} ${classrooms.find(c => c.id === selectedClassroomId)?.name}`
                            : t("select-classroom-to-view")}
                    </p>
                </div>
                <div className="flex items-center gap-3 self-center md:self-end">
                    <div className="bg-indigo-600 rounded-xl p-3 shadow-sm shadow-indigo-100">
                        <FiCalendar className="w-6 h-6 text-white" />
                    </div>
                </div>
            </div>

            {error && (
                <div className="mb-8 p-4 bg-red-50/80 backdrop-blur-sm border border-red-100 text-red-700 rounded-2xl shadow-sm flex items-center gap-3 animate-in fade-in slide-in-from-top-4 duration-500">
                    <FiAlertCircle className="w-5 h-5 shrink-0" />
                    <p className="font-medium text-sm">{error}</p>
                </div>
            )}

            {/* Classroom selector */}
            <div className="bg-white/70 backdrop-blur-xl rounded-2xl shadow-sm p-6 mb-8 border border-white/50 ring-1 ring-gray-200/50">
                <div className="max-w-md mx-auto">
                    <label htmlFor="classroom" className="text-xs font-bold text-gray-700 mb-2 flex items-center gap-2 uppercase tracking-wider">
                        <FiBookOpen className="text-indigo-600 w-3.5 h-3.5" />
                        {t("select-classroom")}
                    </label>

                    {loadingClassrooms ? (
                        <div className="animate-pulse bg-gray-100 h-11 rounded-xl w-full" />
                    ) : classrooms.length === 0 ? (
                        <div className="bg-amber-50 border border-amber-100 rounded-xl p-3 text-amber-700 text-xs italic">
                            {t("no-classrooms-assigned")}
                        </div>
                    ) : (
                        <div className="relative group">
                            <select
                                id="classroom"
                                className="w-full p-3 pl-10 bg-white/50 border-2 border-gray-100 rounded-xl focus:ring-4 focus:ring-indigo-500/10 focus:border-indigo-500 transition-all outline-none text-gray-800 font-bold text-sm appearance-none cursor-pointer shadow-sm hover:border-gray-200"
                                value={selectedClassroomId}
                                onChange={(e) => setSelectedClassroomId(e.target.value)}
                            >
                                <option value="">{t("please-select-classroom")}</option>
                                {classrooms.map((cls) => (
                                    <option key={cls.id} value={cls.id}>{cls.name}</option>
                                ))}
                            </select>
                            <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-indigo-600 transition-colors">
                                <FiSearch className="w-4 h-4" />
                            </div>
                            <div className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none">
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                                </svg>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Attendance table */}
            {selectedClassroomId && (
                <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-sm overflow-hidden border border-white/50 ring-1 ring-gray-200/50 animate-in fade-in slide-in-from-bottom-8 duration-700">
                    <div className="px-6 py-4 border-b border-gray-100/50 bg-gray-50/50 flex items-center justify-between">
                        <h2 className="text-xl font-bold text-gray-900 tracking-tight flex items-center gap-2">
                            <span className="w-1.5 h-6 bg-indigo-600 rounded-full" />
                            {t("attendance-history")}
                        </h2>
                        {attendances.length > 0 && (
                            <span className="bg-indigo-50 text-indigo-700 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider shadow-sm">
                                {attendances.length} {t("records")}
                            </span>
                        )}
                    </div>

                    {loadingAttendances ? (
                        <div className="p-20 flex flex-col justify-center items-center gap-4">
                            <div className="relative">
                                <div className="animate-spin rounded-full h-16 w-16 border-4 border-indigo-100 border-t-indigo-600" />
                                <div className="absolute inset-0 flex items-center justify-center">
                                    <FiClock className="w-6 h-6 text-indigo-200 animate-pulse" />
                                </div>
                            </div>
                            <p className="text-gray-400 font-bold animate-pulse">{t("loading-data")}</p>
                        </div>
                    ) : attendances.length === 0 ? (
                        <div className="p-20 text-center flex flex-col items-center max-w-sm mx-auto">
                            <div className="bg-gray-50 rounded-full p-8 mb-6 border border-gray-100 shadow-inner">
                                <FiSearch className="h-16 w-16 text-gray-300" />
                            </div>
                            <h3 className="text-xl font-bold text-gray-800 mb-2">{t("no-results")}</h3>
                            <p className="text-gray-500 leading-relaxed">{t("no-attendance-data")}</p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full">
                                <thead>
                                    <tr className="bg-gray-50/50">
                                        <th className="px-6 py-4 text-left text-[10px] font-semibold text-gray-400 uppercase tracking-widest border-b border-gray-100">{t("date")}</th>
                                        <th className="px-6 py-4 text-left text-[10px] font-semibold text-gray-400 uppercase tracking-widest border-b border-gray-100">{t("check-in-time")}</th>
                                        <th className="px-6 py-4 text-left text-[10px] font-semibold text-gray-400 uppercase tracking-widest border-b border-gray-100">{t("status")}</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-50">
                                    {attendances.map((attendance, index) => {
                                        const config = getStatusConfig(attendance.status, t);
                                        return (
                                            <tr key={attendance.id || index} className="group hover:bg-gray-50/80 transition-all duration-300">
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="flex items-center gap-3">
                                                        <div className="bg-gray-100 p-2 rounded-xl group-hover:bg-indigo-50 group-hover:text-indigo-600 transition-colors duration-300">
                                                            <FiCalendar className="w-4 h-4" />
                                                        </div>
                                                        <div className="text-sm font-bold text-gray-900 tracking-tight">
                                                            {attendance.attendanceDate
                                                                ? new Date(attendance.attendanceDate).toLocaleDateString(
                                                                    i18n.language === "en" ? "en-US" : "vi-VN",
                                                                    { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' }
                                                                )
                                                                : 'N/A'}
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <div className="inline-flex items-center gap-1.5 px-2 py-1 bg-gray-50 border border-gray-100 rounded-lg text-xs text-gray-700 font-bold group-hover:bg-white group-hover:shadow-sm transition-all duration-300">
                                                        <FiClock className="w-3.5 h-3.5 opacity-50" />
                                                        {attendance.checkInTime || '--:--'}
                                                    </div>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[11px] font-bold border transition-all duration-300 shadow-sm ${config.class}`}>
                                                        {config.icon}
                                                        {config.label}
                                                    </span>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
