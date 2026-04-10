'use client';

import { useState, useCallback } from 'react';
import AcademicProfile, { type SubjectAvg, type SurveyData } from './AcademicProfile';
import HollandTest from './HollandTest';
import ResultsDashboard from './ResultsDashboard';
import ChatWidget from './ChatWidget';
import { guidanceApi, guidanceEndpoints } from '@/lib/utils/api';
import type { HollandCategory } from './HollandQuestions';

/* ───── Step type ───── */
type Step = 'profile' | 'holland' | 'results';

/* ───── Subject name mapping ───── */
const SUBJECT_FIELD_MAP: Record<string, string> = {
    'Toán': 'math_score', 'Toán học': 'math_score',
    'Vật lý': 'physics_score', 'Lý': 'physics_score',
    'Hóa học': 'chemistry_score', 'Hóa': 'chemistry_score',
    'Sinh học': 'biology_score', 'Sinh': 'biology_score',
    'Lịch sử': 'history_score', 'Sử': 'history_score',
    'Tiếng Anh': 'english_score', 'Anh': 'english_score',
    'Địa lý': 'geography_score', 'Địa': 'geography_score',
};

function mapScoresToApi(scores: SubjectAvg[]): Record<string, number> {
    const result: Record<string, number> = {
        math_score: 0, history_score: 0, physics_score: 0,
        chemistry_score: 0, biology_score: 0, english_score: 0, geography_score: 0,
    };
    for (const s of scores) {
        const field = SUBJECT_FIELD_MAP[s.name];
        if (field) result[field] = s.score;
    }
    return result;
}

export default function OrientationClient() {
    /* ── State ── */
    const [step, setStep] = useState<Step>('profile');
    const [isLoading, setIsLoading] = useState(false);

    // Data from Module 1
    const [savedScores, setSavedScores] = useState<SubjectAvg[]>([]);
    const [savedSurvey, setSavedSurvey] = useState<SurveyData | null>(null);

    // API results
    const [academicResult, setAcademicResult] = useState<string | null>(null);
    const [hollandResult, setHollandResult] = useState<string | null>(null);
    const [sessionId, setSessionId] = useState<string | null>(null);

    // Holland test data
    const [isHollandCompleted, setIsHollandCompleted] = useState(false);
    const [hollandScores, setHollandScores] = useState<Record<HollandCategory, number> | null>(null);

    /* ── Module 1: Submit academic + survey data ── */
    const handleAcademicSubmit = useCallback(async (scores: SubjectAvg[], survey: SurveyData) => {
        setSavedScores(scores);
        setSavedSurvey(survey);
        setIsLoading(true);

        try {
            const scoreFields = mapScoresToApi(scores);
            const res = await guidanceApi.post(guidanceEndpoints.academicGuidance, {
                scores: scoreFields,
                profile: {
                    gender: survey.gender,
                    absences: survey.absences,
                },
                survey: {
                    has_part_time_job: survey.has_part_time_job,
                    extracurricular_activities: survey.extracurricular_activities,
                    self_study_hours: survey.self_study_hours,
                },
            });

            setAcademicResult(res.data.academic_result);
            setSessionId(res.data.session_id);
            setStep('results');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (err) {
            console.error('Academic guidance error:', err);
            alert('Không thể kết nối với hệ thống AI. Vui lòng kiểm tra kết nối và thử lại.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    /* ── Module 2: Holland test completes ── */
    const handleHollandComplete = useCallback(async (
        codes: [HollandCategory, HollandCategory, HollandCategory],
        scores: Record<HollandCategory, number>,
    ) => {
        setHollandScores(scores);
        setIsLoading(true);

        try {
            // If we already have academic data, call full endpoint
            if (savedSurvey && savedScores.length > 0) {
                const scoreFields = mapScoresToApi(savedScores);
                const res = await guidanceApi.post(guidanceEndpoints.fullGuidance, {
                    holland_codes: { code1: codes[0], code2: codes[1], code3: codes[2] },
                    scores: scoreFields,
                    profile: {
                        gender: savedSurvey.gender,
                        absences: savedSurvey.absences,
                    },
                    survey: {
                        has_part_time_job: savedSurvey.has_part_time_job,
                        extracurricular_activities: savedSurvey.extracurricular_activities,
                        self_study_hours: savedSurvey.self_study_hours,
                    },
                });

                setAcademicResult(res.data.academic_result);
                setHollandResult(res.data.holland_result);
                setSessionId(res.data.session_id);
            } else {
                // Holland only
                const res = await guidanceApi.post(guidanceEndpoints.hollandGuidance, {
                    holland_codes: { code1: codes[0], code2: codes[1], code3: codes[2] },
                });

                setHollandResult(res.data.holland_result);
                setSessionId(res.data.session_id);
            }

            setIsHollandCompleted(true);
            setStep('results');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (err) {
            console.error('Holland guidance error:', err);
            alert('Không thể kết nối với hệ thống AI. Vui lòng thử lại.');
        } finally {
            setIsLoading(false);
        }
    }, [savedScores, savedSurvey]);

    /* ── Holland skip ── */
    const handleHollandSkip = useCallback(() => {
        setStep('results');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, []);

    /* ── Navigate to Holland ── */
    const handleStartHolland = useCallback(() => {
        setStep('holland');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, []);

    /* ── Navigate back to profile ── */
    const handleBackToProfile = useCallback(() => {
        setStep('profile');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, []);

    return (
        <div className="container mx-auto px-4 py-8 max-w-3xl min-h-screen">
            {/* ══════ Step indicator ══════ */}
            <div className="mb-8">
                <div className="flex items-center justify-center gap-2">
                    {[
                        { key: 'profile', label: 'Hồ sơ', active: step === 'profile' },
                        { key: 'holland', label: 'Holland', active: step === 'holland' },
                        { key: 'results', label: 'Kết quả', active: step === 'results' },
                    ].map((s, i) => (
                        <div key={s.key} className="flex items-center gap-3">
                            {i > 0 && <div className="w-6 h-px bg-gray-200" />}
                            <div className={`text-[10px] font-bold uppercase tracking-widest transition-all ${s.active
                                ? 'text-gray-900 border-b-2 border-gray-900 pb-1'
                                : 'text-gray-300'
                                }`}>
                                {s.label}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* ══════ Step Content ══════ */}
            {step === 'profile' && (
                <AcademicProfile
                    onSubmit={handleAcademicSubmit}
                    isLoading={isLoading}
                />
            )}

            {step === 'holland' && (
                <HollandTest
                    onComplete={handleHollandComplete}
                    onSkip={handleHollandSkip}
                    isLoading={isLoading}
                />
            )}

            {step === 'results' && (
                <>
                    <ResultsDashboard
                        academicResult={academicResult}
                        hollandResult={hollandResult}
                        isHollandCompleted={isHollandCompleted}
                        hollandScores={hollandScores}
                        onStartHolland={handleStartHolland}
                    />

                    {/* Back button */}
                    <div className="mt-6 text-center">
                        <button
                            onClick={handleBackToProfile}
                            className="text-sm text-gray-400 hover:text-indigo-600 font-medium transition-colors"
                        >
                            ← Quay lại hồ sơ học tập
                        </button>
                    </div>
                </>
            )}

            {/* ══════ Floating Chat Widget (always present when session exists) ══════ */}
            <ChatWidget sessionId={sessionId} />
        </div>
    );
}
