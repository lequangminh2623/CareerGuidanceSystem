'use client';

import { useCallback } from 'react';
import AcademicProfile from './AcademicProfile';
import HollandTest from './HollandTest';
import ResultsDashboard from './ResultsDashboard';
import ChatWidget from './ChatWidget';
import { useTranslation } from 'react-i18next';
import { guidanceApi, guidanceEndpoints } from '@/lib/utils/api';
import type { HollandCategory } from './HollandQuestions';
import { useAppSelector, useAppDispatch } from '@/store/hooks';
import {
    setStep,
    setLoading,
    setSavedData,
    setAcademicResult,
    setHollandResult,
    setSessionId,
    setHollandCompleted,
} from '@/store/features/orientation/orientationSlice';
import { showNotification } from '@/store/features/ui/uiSlice';
import type { SubjectAvg, SurveyData } from '@/store/features/orientation/orientationSlice';

/* ───── Subject name mapping ───── */
const SUBJECT_FIELD_MAP: Record<string, string> = {
    'Toán': 'math_score', 'Toán học': 'math_score',
    'Vật lý': 'physics_score', 'Lý': 'physics_score',
    'Hóa học': 'chemistry_score', 'Hóa': 'chemistry_score',
    'Sinh học': 'biology_score', 'Sinh': 'biology_score',
    'Lịch sử': 'history_score', 'Sử': 'history_score',
    'Tiếng Anh': 'english_score', 'Anh': 'english_score',
    'Địa lý': 'geography_score', 'Địa': 'geography_score',
    'Math': 'math_score', 'MATH': 'math_score',
    'Physics': 'physics_score', 'PHYSICS': 'physics_score',
    'Chemistry': 'chemistry_score', 'CHEMISTRY': 'chemistry_score',
    'Biology': 'biology_score', 'BIOLOGY': 'biology_score',
    'History': 'history_score', 'HISTORY': 'history_score',
    'English': 'english_score', 'ENGLISH': 'english_score',
    'Geography': 'geography_score', 'GEOGRAPHY': 'geography_score',
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
    const { t } = useTranslation();
    const dispatch = useAppDispatch();

    // All state from Redux — no more useState for these
    const { step, isLoading, savedScores, savedSurvey, academicResult } = useAppSelector(
        (state) => state.orientation
    );

    /* ── Module 1: Submit academic + survey data ── */
    const handleAcademicSubmit = useCallback(async (scores: SubjectAvg[], survey: SurveyData) => {
        dispatch(setSavedData({ scores, survey }));
        dispatch(setLoading(true));

        try {
            const scoreFields = mapScoresToApi(scores);
            const res = await guidanceApi.post(guidanceEndpoints.academicGuidance, {
                scores: scoreFields,
                profile: { gender: survey.gender, absences: survey.absences },
                survey: {
                    has_part_time_job: survey.has_part_time_job,
                    extracurricular_activities: survey.extracurricular_activities,
                    self_study_hours: survey.self_study_hours,
                },
            });

            dispatch(setAcademicResult(res.data.academic_result));
            dispatch(setSessionId(res.data.session_id));
            dispatch(setStep('results'));
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (err) {
            console.error('Academic guidance error:', err);
            dispatch(showNotification({
                type: 'error',
                title: 'Lỗi hệ thống',
                message: 'Không thể kết nối với hệ thống AI. Vui lòng kiểm tra kết nối và thử lại.',
            }));
        } finally {
            dispatch(setLoading(false));
        }
    }, [dispatch]);

    /* ── Module 2: Holland test completes ── */
    const handleHollandComplete = useCallback(async (
        codes: [HollandCategory, HollandCategory, HollandCategory],
        scores: Record<HollandCategory, number>,
    ) => {
        dispatch(setHollandCompleted({ scores }));
        dispatch(setLoading(true));

        try {
            if (academicResult) {
                const res = await guidanceApi.post(guidanceEndpoints.hollandGuidance, {
                    holland_codes: { code1: codes[0], code2: codes[1], code3: codes[2] },
                });
                dispatch(setHollandResult(res.data.holland_result));
                dispatch(setSessionId(res.data.session_id));
            } else if (savedSurvey && savedScores.length > 0) {
                const scoreFields = mapScoresToApi(savedScores);
                const res = await guidanceApi.post(guidanceEndpoints.fullGuidance, {
                    holland_codes: { code1: codes[0], code2: codes[1], code3: codes[2] },
                    scores: scoreFields,
                    profile: { gender: savedSurvey.gender, absences: savedSurvey.absences },
                    survey: {
                        has_part_time_job: savedSurvey.has_part_time_job,
                        extracurricular_activities: savedSurvey.extracurricular_activities,
                        self_study_hours: savedSurvey.self_study_hours,
                    },
                });
                dispatch(setAcademicResult(res.data.academic_result));
                dispatch(setHollandResult(res.data.holland_result));
                dispatch(setSessionId(res.data.session_id));
            } else {
                const res = await guidanceApi.post(guidanceEndpoints.hollandGuidance, {
                    holland_codes: { code1: codes[0], code2: codes[1], code3: codes[2] },
                });
                dispatch(setHollandResult(res.data.holland_result));
                dispatch(setSessionId(res.data.session_id));
            }

            dispatch(setStep('results'));
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch (err) {
            console.error('Holland guidance error:', err);
            dispatch(showNotification({
                type: 'error',
                title: 'Lỗi hệ thống',
                message: 'Không thể kết nối với hệ thống AI. Vui lòng thử lại.',
            }));
        } finally {
            dispatch(setLoading(false));
        }
    }, [dispatch, academicResult, savedScores, savedSurvey]);

    /* ── Holland skip ── */
    const handleHollandSkip = useCallback(() => {
        dispatch(setStep('results'));
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [dispatch]);

    /* ── Navigate back to profile ── */
    const handleBackToProfile = useCallback(() => {
        dispatch(setStep('profile'));
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [dispatch]);

    return (
        <div className="container mx-auto px-4 py-8 max-w-3xl min-h-screen">
            {/* ══════ Step indicator ══════ */}
            <div className="mb-8">
                <div className="flex items-center justify-center gap-2">
                    {[
                        { key: 'profile', label: t('orientation-steps-academic'), active: step === 'profile' },
                        { key: 'holland', label: t('orientation-steps-riasec'), active: step === 'holland' },
                        { key: 'results', label: t('orientation-steps-results'), active: step === 'results' },
                    ].map((s, i) => (
                        <div key={s.key} className="flex items-center gap-3">
                            {i > 0 && <div className="w-6 h-px bg-gray-200" />}
                            <div className={`text-[10px] font-bold uppercase tracking-widest transition-all ${s.active
                                ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
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
                    {/* ResultsDashboard reads all data from Redux — no props */}
                    <ResultsDashboard />

                    {/* Back button */}
                    <div className="mt-6 text-center">
                        <button
                            onClick={handleBackToProfile}
                            className="text-sm text-gray-400 hover:text-indigo-600 font-medium transition-colors"
                        >
                            ← {t('back')}
                        </button>
                    </div>
                </>
            )}

            {/* ══════ Floating Chat Widget — reads sessionId from Redux ══════ */}
            <ChatWidget />
        </div>
    );
}
