'use client';

import { useEffect, useState } from 'react';
import { authApis, endpoints } from '@/lib/utils/api';
import { FiBookOpen, FiClock, FiBriefcase, FiUsers, FiArrowRight } from 'react-icons/fi';

/* ───── Types ───── */
interface StudentScoreDTO {
    id: string;
    midtermScore: number | null;
    finalScore: number | null;
    subjectName: string;
    semesterName: string;
    yearName: string;
}

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

/* ───── Subject icon map ───── */
const getIcon = (name: string) => '';

/* ───── Component ───── */
export default function AcademicProfile({ onSubmit, isLoading }: Props) {
    const [scores, setScores] = useState<SubjectAvg[]>([]);
    const [loadingScores, setLoadingScores] = useState(true);
    const [errorMsg, setErrorMsg] = useState('');

    const [survey, setSurvey] = useState<SurveyData>({
        has_part_time_job: false,
        extracurricular_activities: false,
        self_study_hours: 10,
        gender: 'nam',
        absences: 0,
    });

    /* ── Fetch scores from Spring Boot ── */
    useEffect(() => {
        const fetchScores = async () => {
            try {
                setLoadingScores(true);
                const res = await authApis().get(endpoints['student-scores']);
                const data: StudentScoreDTO[] = res.data;

                if (!data || !Array.isArray(data)) { setScores([]); return; }

                // Aggregate per subject
                const subjectMap: Record<string, number[]> = {};
                for (const s of data) {
                    const name = s.subjectName || '';
                    if (!subjectMap[name]) subjectMap[name] = [];
                    const mid = s.midtermScore ?? 0;
                    const fin = s.finalScore ?? 0;
                    subjectMap[name].push(Math.round(((mid + fin) / 2) * 10) / 10);
                }

                const avgList: SubjectAvg[] = Object.entries(subjectMap).map(([name, vals]) => ({
                    name,
                    score: Math.round((vals.reduce((a, b) => a + b, 0) / vals.length) * 10) / 10,
                    icon: getIcon(name),
                }));

                setScores(avgList);
            } catch {
                setErrorMsg('Không thể tải dữ liệu điểm số. Vui lòng thử lại.');
            } finally {
                setLoadingScores(false);
            }
        };
        fetchScores();
    }, []);

    const handleSubmit = () => {
        onSubmit(scores, survey);
    };

    /* ── Score color gradient ── */
    const scoreColor = (v: number) => {
        if (v >= 8) return 'from-emerald-500 to-emerald-600';
        if (v >= 6.5) return 'from-blue-500 to-blue-600';
        if (v >= 5) return 'from-amber-500 to-amber-600';
        return 'from-red-500 to-red-600';
    };

    return (
        <div className="space-y-8 animate-in fade-in duration-700">
            {/* ══════ Header ══════ */}
            <div className="text-center space-y-2">
                <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Hồ sơ học tập</h2>
                <p className="text-gray-500 max-w-lg mx-auto text-sm">
                    Phân tích kết quả học tập và thói quen để nhận tư vấn hướng nghiệp từ AI.
                </p>
            </div>

            {/* ══════ Score Display ══════ */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
                    <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest">
                        Bảng điểm trung bình
                    </h3>
                </div>

                <div className="p-6">
                    {loadingScores ? (
                        <div className="flex justify-center py-12">
                            <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" />
                        </div>
                    ) : errorMsg ? (
                        <div className="text-center py-8">
                            <p className="text-red-500 font-medium">{errorMsg}</p>
                        </div>
                    ) : scores.length === 0 ? (
                        <div className="text-center py-8 text-gray-400">
                            <p className="font-medium">Chưa có dữ liệu điểm số</p>
                            <p className="text-sm mt-1">Điểm số sẽ được cập nhật khi giáo viên nhập liệu.</p>
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
                        Thông tin bổ sung
                    </h3>
                </div>

                <div className="p-6 space-y-6">
                    {/* Gender */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">Giới tính</label>
                        <div className="flex gap-3">
                            {[{ v: 'nam', l: 'Nam' }, { v: 'nữ', l: 'Nữ' }].map((g) => (
                                <button
                                    key={g.v}
                                    type="button"
                                    onClick={() => setSurvey(p => ({ ...p, gender: g.v }))}
                                    className={`flex-1 py-3 px-4 rounded-xl border-2 text-sm font-semibold transition-all ${survey.gender === g.v
                                        ? 'border-indigo-500 bg-indigo-50 text-indigo-700 shadow-sm'
                                        : 'border-gray-200 text-gray-600 hover:border-indigo-200 hover:bg-indigo-50/30'
                                        }`}
                                >
                                    {g.l}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Part-time job */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">
                            <FiBriefcase className="inline mr-1.5 text-indigo-500" />
                            Bạn có đang đi làm thêm không?
                        </label>
                        <div className="flex gap-3">
                            {[{ v: false, l: 'Không' }, { v: true, l: 'Có' }].map((o) => (
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
                            Bạn có tham gia hoạt động ngoại khóa không?
                        </label>
                        <div className="flex gap-3">
                            {[{ v: false, l: 'Không' }, { v: true, l: 'Có' }].map((o) => (
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
                            Số giờ tự học mỗi tuần
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                min={0}
                                max={100}
                                value={survey.self_study_hours}
                                onChange={(e) => setSurvey(p => ({ ...p, self_study_hours: Math.max(0, parseInt(e.target.value) || 0) }))}
                                className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all text-gray-900 font-medium"
                                placeholder="Ví dụ: 15"
                            />
                            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm text-gray-400 font-medium">giờ/tuần</span>
                        </div>
                    </div>

                    {/* Absences */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">
                            Số ngày vắng mặt
                        </label>
                        <input
                            type="number"
                            min={0}
                            value={survey.absences}
                            onChange={(e) => setSurvey(p => ({ ...p, absences: Math.max(0, parseInt(e.target.value) || 0) }))}
                            className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all text-gray-900 font-medium"
                            placeholder="Ví dụ: 3"
                        />
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
                        Đang phân tích...
                    </>
                ) : (
                    <>
                        Nhận tư vấn ngay
                        <FiArrowRight className="w-5 h-5" />
                    </>
                )}
            </button>
        </div>
    );
}
