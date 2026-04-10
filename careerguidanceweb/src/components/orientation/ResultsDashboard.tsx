'use client';

import { useEffect, useRef } from 'react';
import ReactMarkdown from 'react-markdown';
import { Chart, registerables } from 'chart.js';
import { HOLLAND_CATEGORY_LABELS, type HollandCategory } from './HollandQuestions';
import { FiAward, FiTrendingUp, FiUnlock, FiArrowRight } from 'react-icons/fi';

Chart.register(...registerables);

interface Props {
    academicResult: string | null;
    hollandResult: string | null;
    isHollandCompleted: boolean;
    hollandScores: Record<HollandCategory, number> | null;
    onStartHolland: () => void;
}

export default function ResultsDashboard({
    academicResult,
    hollandResult,
    isHollandCompleted,
    hollandScores,
    onStartHolland,
}: Props) {
    const chartRef = useRef<HTMLCanvasElement>(null);
    const chartInstanceRef = useRef<Chart | null>(null);

    /* ── Radar Chart ── */
    useEffect(() => {
        if (!chartRef.current || !hollandScores) return;

        if (chartInstanceRef.current) {
            chartInstanceRef.current.destroy();
        }

        const categories = Object.keys(HOLLAND_CATEGORY_LABELS) as HollandCategory[];
        const labels = categories.map(c => `${HOLLAND_CATEGORY_LABELS[c].vi} (${c})`);
        const data = categories.map(c => hollandScores[c]);
        const colors = categories.map(c => HOLLAND_CATEGORY_LABELS[c].color);

        chartInstanceRef.current = new Chart(chartRef.current, {
            type: 'radar',
            data: {
                labels,
                datasets: [{
                    label: 'Điểm RIASEC',
                    data,
                    backgroundColor: 'rgba(99, 102, 241, 0.15)',
                    borderColor: 'rgba(99, 102, 241, 0.8)',
                    borderWidth: 2.5,
                    pointBackgroundColor: colors,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8,
                }],
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                scales: {
                    r: {
                        beginAtZero: true,
                        max: 50,
                        ticks: {
                            stepSize: 10,
                            font: { size: 10 },
                            backdropColor: 'transparent',
                        },
                        pointLabels: {
                            font: { size: 11, weight: 'bold' as const },
                            color: '#374151',
                        },
                        grid: { color: 'rgba(0,0,0,0.06)' },
                        angleLines: { color: 'rgba(0,0,0,0.06)' },
                    },
                },
                plugins: {
                    legend: { display: false },
                },
                animation: { duration: 1200, easing: 'easeOutQuart' },
            },
        });

        return () => { chartInstanceRef.current?.destroy(); };
    }, [hollandScores]);

    return (
        <div className="space-y-8 animate-in fade-in duration-700">
            {/* ══════ Header ══════ */}
            <div className="text-center space-y-2">
                <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Kết quả tư vấn AI</h2>
                <p className="text-gray-500 max-w-lg mx-auto text-sm">
                    {isHollandCompleted
                        ? 'Phân tích định hướng nghề nghiệp dựa trên năng lực và tính cách.'
                        : 'Phân tích định hướng dựa trên kết quả học tập.'}
                </p>
            </div>

            {/* ══════ Holland Radar (if completed) ══════ */}
            {isHollandCompleted && hollandScores && (
                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                        <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                            Biểu đồ tính cách RIASEC
                        </h3>
                    </div>
                    <div className="p-6 flex justify-center">
                        <div className="w-full max-w-md">
                            <canvas ref={chartRef} />
                        </div>
                    </div>
                    {/* Top 3 codes */}
                    <div className="px-6 pb-6">
                        <div className="flex gap-3 justify-center">
                            {(Object.entries(hollandScores) as [HollandCategory, number][])
                                .sort((a, b) => b[1] - a[1])
                                .slice(0, 3)
                                .map(([code, score], i) => {
                                    const info = HOLLAND_CATEGORY_LABELS[code];
                                    return (
                                        <div key={code} className="text-center px-5 py-3 rounded-xl border-2" style={{ borderColor: info.color + '20', backgroundColor: info.color + '05' }}>
                                            <div className="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                                                Top {i + 1}
                                            </div>
                                            <div className="text-sm font-bold" style={{ color: info.color }}>
                                                {info.vi} ({code})
                                            </div>
                                            <div className="text-lg font-extrabold text-gray-900">{score}</div>
                                        </div>
                                    );
                                })}
                        </div>
                    </div>
                </div>
            )}

            {/* ══════ Holland Result ══════ */}
            {isHollandCompleted && hollandResult && (
                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                        <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                            Định hướng theo Holland
                        </h3>
                    </div>
                    <div className="p-6 prose prose-sm max-w-none text-gray-700 prose-headings:text-gray-900 prose-strong:text-gray-900 prose-li:marker:text-indigo-400">
                        <ReactMarkdown>{hollandResult}</ReactMarkdown>
                    </div>
                </div>
            )}

            {/* ══════ Academic Result ══════ */}
            {academicResult && (
                <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                        <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                            Phân tích năng lực học tập
                        </h3>
                    </div>
                    <div className="p-6 prose prose-sm max-w-none text-gray-700 prose-headings:text-gray-900 prose-strong:text-gray-900 prose-li:marker:text-indigo-400">
                        <ReactMarkdown>{academicResult}</ReactMarkdown>
                    </div>
                </div>
            )}

            {/* ══════ Holland CTA (if not completed) ══════ */}
            {!isHollandCompleted && (
                <div className="relative overflow-hidden rounded-2xl border-2 border-dashed border-indigo-200 bg-linear-to-br from-indigo-50 via-violet-50 to-purple-50 p-8">
                    <div className="absolute top-0 right-0 w-32 h-32 bg-linear-to-br from-indigo-200/30 to-violet-200/30 rounded-full -translate-y-1/2 translate-x-1/2" />
                    <div className="relative flex flex-col items-center text-center space-y-4">
                        <div className="w-14 h-14 rounded-2xl bg-linear-to-br from-amber-400 to-orange-500 flex items-center justify-center shadow-lg">
                            <FiUnlock className="w-7 h-7 text-white" />
                        </div>
                        <h3 className="text-lg font-extrabold text-gray-900">
                            Mở khóa gợi ý nghề nghiệp chuyên sâu
                        </h3>
                        <p className="text-sm text-gray-600 max-w-md">
                            Làm bài trắc nghiệm Holland để AI thấu hiểu tính cách và đưa ra danh sách nghề nghiệp cụ thể phù hợp nhất với bạn.
                        </p>
                        <button
                            onClick={onStartHolland}
                            className="px-6 py-3 bg-gray-900 text-white font-bold rounded-xl shadow-lg hover:bg-gray-800 transition-all duration-300 hover:-translate-y-0.5 flex items-center gap-2"
                        >
                            Làm bài trắc nghiệm Holland
                            <FiArrowRight className="w-4 h-4" />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
