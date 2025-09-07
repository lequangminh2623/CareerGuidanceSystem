'use client';

import { useEffect, useState } from "react";
import { Chart, registerables } from "chart.js";
import { authApis, endpoints } from "@/lib/utils/api";
import { useTranslation } from "react-i18next";
import MySpinner from "../layout/MySpinner";

Chart.register(...registerables);
let chartInstance: Chart | null = null;

interface Semester {
    id: string;
    academicYear: {
        year: string;
    };
    semesterType: string;
}

interface WeakStudent {
    studentId: string;
    studentCode: string;
    fullName: string;
    courseName: string;
}

interface AnalysisResult {
    courseWeakRatios: Record<string, number>;
    criticalCourses: string[];
    weakStudentList: WeakStudent[];
}

const StatisticsClient = () => {
    const [semesters, setSemesters] = useState<Semester[]>([]);
    const [semesterId, setSemesterId] = useState<string>("1");
    const [result, setResult] = useState<AnalysisResult | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const { t } = useTranslation();

    useEffect(() => {
        // Fetch initial data (semesters and analysis for the default semester)
        const fetchInitialData = async () => {
            try {
                setLoading(true);
                const res = await authApis().get(endpoints["analysis"](semesterId));
                const { analysisResult, semesters } = res.data;

                setSemesters(semesters || []);
                if (semesters?.length > 0 && !semesterId) {
                    setSemesterId(semesters[0].id); // Default to the first semester
                }
                setResult(analysisResult);

                // Render the chart after the result is set
                if (analysisResult?.courseWeakRatios) {
                    renderChart(analysisResult.courseWeakRatios);
                }
            } catch (err) {
                console.error("Error fetching initial data:", err);
                setSemesters([]);
                setResult(null);
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, []);

    const fetchAnalysis = async () => {
        try {
            setLoading(true);
            const res = await authApis().get(endpoints["analysis"](semesterId));
            const { analysisResult } = res.data;
            setResult(analysisResult);

            // Render the chart after the result is set
            if (analysisResult?.courseWeakRatios) {
                renderChart(analysisResult.courseWeakRatios);
            }
        } catch (err) {
            console.error("Error fetching analysis:", err);
            setResult(null);
        } finally {
            setLoading(false);
        }
    };

    const renderChart = (courseWeakRatios: Record<string, number>) => {
        setTimeout(() => {
            const canvas = document.getElementById("weakCourseChart") as HTMLCanvasElement;
            if (!canvas) {
                console.error("Canvas element not found. Skipping chart rendering.");
                return;
            }

            const ctx = canvas.getContext("2d");

            if (!ctx) {
                console.error("2D context not available. Skipping chart rendering.");
                return;
            }

            // Destroy the existing chart instance if it exists
            if (chartInstance) {
                chartInstance.destroy();
            }

            const labels = Object.keys(courseWeakRatios);
            const data = Object.values(courseWeakRatios);

            // Create a new chart instance and store it
            chartInstance = new Chart(ctx, {
                type: "bar",
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: t('percentage-weak-students'),
                            data: data,
                            backgroundColor: "rgba(255, 99, 132, 0.6)",
                            borderColor: "rgba(255, 99, 132, 1)",
                            borderWidth: 1,
                        },
                    ],
                },
                options: {
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                        },
                    },
                    plugins: {
                        legend: {
                            display: false,
                        },
                    },
                },
            });
        }, 500); // Delay execution to ensure the DOM is updated
    };

    if (loading) {
        return (
            <MySpinner />
        );
    }

    return (
        <div className="container mx-auto p-4 min-h-screen">
            <form onSubmit={(e) => { e.preventDefault(); fetchAnalysis(); }}>
                <h2 className="text-2xl font-bold mb-4">{t('statistics')}</h2>

                <div className="max-w-md">
                    <div className="mb-4">
                        <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="semesterSelect">
                            {t('semester')}:
                        </label>
                        <select
                            id="semesterSelect"
                            value={semesterId}
                            onChange={(e) => setSemesterId(e.target.value)}
                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                        >
                            {semesters?.map((s) => (
                                <option key={s.id} value={s.id}>
                                    {`${s.academicYear.year} - ${s.semesterType}`}
                                </option>
                            )) || []}
                        </select>
                    </div>

                    <button type="submit"
                        className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-primary-dark transition-colors">
                        {t('view-statistics')}
                    </button>
                </div>
            </form>

            {result && (
                <div className="mt-8">
                    <h3 className="text-xl font-semibold mb-4">{t('chart-title')}</h3>
                    <div className="bg-white p-4 rounded-lg shadow">
                        <canvas id="weakCourseChart" height="100"></canvas>
                    </div>

                    <h3 className="text-xl font-semibold mt-8 mb-4">{t('list-courses')} (&ge;40%)</h3>
                    <ul className="list-disc pl-5 space-y-2">
                        {result.criticalCourses?.map((course, index) => (
                            <li key={index} className="text-gray-700">{course}</li>
                        )) || []}
                    </ul>

                    <h3 className="text-xl font-semibold mt-8 mb-4">{t('list-students')}</h3>
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t('student-code')}
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t('full-name')}
                                    </th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                        {t('course')}
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {result.weakStudentList?.map((student) => (
                                    <tr key={`${student.studentId}-${student.courseName}`}
                                        className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">{student.studentCode}</td>
                                        <td className="px-6 py-4 whitespace-nowrap">{student.fullName}</td>
                                        <td className="px-6 py-4 whitespace-nowrap">{student.courseName}</td>
                                    </tr>
                                )) || []}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

export default StatisticsClient;
