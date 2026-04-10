'use client';

import { useContext } from "react";
import { MyUserContext } from "@/lib/contexts/userContext";
import StudentStatistics from "./StudentStatistics";
import TeacherStatistics from "./TeacherStatistics";
import { useTranslation } from "react-i18next";
import { FiBarChart2 } from "react-icons/fi";

const StatisticsClient = () => {
    const user = useContext(MyUserContext);
    const { t } = useTranslation();
    const role = user?.role;

    return (
        <div className="container mx-auto px-4 py-8 max-w-7xl min-h-screen">
            {/* Header */}
            <div className="mb-8">
                <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight flex items-center gap-3">
                    <FiBarChart2 className="text-blue-600 h-8 w-8" />
                    {t('statistics-header')}
                </h1>
                <p className="mt-2 text-sm text-gray-500">
                    {role === "Student"
                        ? t("Student-stats-desc")
                        : role === "Teacher"
                            ? t("Teacher-stats-desc")
                            : t("general-stats-desc")}
                </p>
            </div>

            {/* Role-based content */}
            {role === "Student" && <StudentStatistics />}
            {role === "Teacher" && <TeacherStatistics />}
            {role !== "Student" && role !== "Teacher" && (
                <div className="bg-white border border-gray-100 rounded-2xl shadow-sm p-12 text-center flex flex-col items-center justify-center max-w-2xl mx-auto">
                    <div className="bg-blue-50 p-4 rounded-full mb-4">
                        <FiBarChart2 className="h-8 w-8 text-blue-500" />
                    </div>
                    <h3 className="text-lg font-bold text-gray-900 mb-2">
                        {t('stats-feature')}
                    </h3>
                    <p className="text-gray-500 text-sm max-w-sm">
                        {t('stats-restricted')}
                    </p>
                </div>
            )}
        </div>
    );
};

export default StatisticsClient;
