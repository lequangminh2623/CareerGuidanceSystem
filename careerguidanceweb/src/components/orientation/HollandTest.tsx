'use client';

import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
    HOLLAND_QUESTIONS, LIKERT_OPTIONS, HOLLAND_CATEGORY_LABELS,
    calculateRIASEC, getTop3Codes,
    type HollandCategory,
} from './HollandQuestions';
import { FiArrowLeft, FiArrowRight, FiSkipForward, FiCheck } from 'react-icons/fi';

const QUESTIONS_PER_PAGE = 10;
const TOTAL_PAGES = Math.ceil(HOLLAND_QUESTIONS.length / QUESTIONS_PER_PAGE);

interface Props {
    onComplete: (codes: [HollandCategory, HollandCategory, HollandCategory], scores: Record<HollandCategory, number>) => void;
    onSkip: () => void;
    isLoading: boolean;
}

export default function HollandTest({ onComplete, onSkip, isLoading }: Props) {
    const { t, i18n } = useTranslation();
    const [page, setPage] = useState(0);
    const [answers, setAnswers] = useState<Record<number, number>>({});

    const currentQuestions = useMemo(
        () => HOLLAND_QUESTIONS.slice(page * QUESTIONS_PER_PAGE, (page + 1) * QUESTIONS_PER_PAGE),
        [page]
    );

    const totalAnswered = Object.keys(answers).length;
    const progress = (totalAnswered / HOLLAND_QUESTIONS.length) * 100;

    const pageFullyAnswered = currentQuestions.every(q => answers[q.id] !== undefined);

    const handleAnswer = (questionId: number, value: number) => {
        setAnswers(prev => ({ ...prev, [questionId]: value }));
    };

    const handleNext = () => {
        if (page < TOTAL_PAGES - 1) {
            setPage(p => p + 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    const handlePrev = () => {
        if (page > 0) {
            setPage(p => p - 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    const handleFinish = () => {
        const scores = calculateRIASEC(answers);
        const top3 = getTop3Codes(scores);
        onComplete(top3, scores);
    };

    const isLastPage = page === TOTAL_PAGES - 1;

    return (
        <div className="space-y-6 animate-in fade-in duration-700">
            {/* ══════ Header ══════ */}
            <div className="text-center space-y-2">
                <h2 className="text-3xl font-extrabold text-blue-600 tracking-tight">{t('riasec-test-title')}</h2>
                <p className="text-gray-500 max-w-lg mx-auto text-sm">
                    {t('riasec-test-desc')}
                </p>
            </div>

            {/* ══════ Progress Bar ══════ */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
                <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-bold text-gray-700">
                        {t('question-progress', { current: page + 1, total: TOTAL_PAGES })}
                    </span>
                    <span className="text-sm font-bold text-indigo-600">
                        {totalAnswered}/{HOLLAND_QUESTIONS.length} {t('records')}
                    </span>
                </div>
                <div className="h-3 bg-gray-100 rounded-full overflow-hidden">
                    <div
                        className="h-full rounded-full bg-linear-to-r from-indigo-500 via-violet-500 to-purple-500 transition-all duration-500 ease-out"
                        style={{ width: `${progress}%` }}
                    />
                </div>
                {/* Category dots */}
                <div className="flex justify-between mt-3">
                    {(Object.entries(HOLLAND_CATEGORY_LABELS) as [HollandCategory, typeof HOLLAND_CATEGORY_LABELS.R][]).map(([code, info]) => (
                        <div key={code} className="flex items-center gap-1.5">
                            <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: info.color }} />
                            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">{code}</span>
                        </div>
                    ))}
                </div>
            </div>

            {/* ══════ Questions ══════ */}
            <div className="space-y-4">
                {currentQuestions.map((q, idx) => {
                    const catInfo = HOLLAND_CATEGORY_LABELS[q.category];
                    return (
                        <div
                            key={q.id}
                            className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:shadow-md transition-all duration-300"
                        >
                            <div className="flex items-start gap-3 mb-4">
                                <div
                                    className="shrink-0 w-8 h-8 rounded-lg flex items-center justify-center text-white text-sm font-bold"
                                    style={{ backgroundColor: catInfo.color }}
                                >
                                    {page * QUESTIONS_PER_PAGE + idx + 1}
                                </div>
                                <div>
                                    <p className="text-sm font-bold text-gray-800 leading-snug">{t(`riasec-q${q.id}`)}</p>
                                    <span className="text-[10px] px-2 py-0.5 rounded-full mt-1 inline-block font-bold uppercase tracking-wider"
                                        style={{ backgroundColor: catInfo.color + '10', color: catInfo.color }}>
                                        {i18n.language === 'en' ? catInfo.en : catInfo.vi}
                                    </span>
                                </div>
                            </div>

                            {/* Likert buttons */}
                            <div className="flex gap-2">
                                {LIKERT_OPTIONS.map((opt) => {
                                    const likertKeys = [
                                        'likert-very-dislike',
                                        'likert-dislike',
                                        'likert-neutral',
                                        'likert-like',
                                        'likert-very-like'
                                    ];
                                    return (
                                        <button
                                            key={opt.value}
                                            type="button"
                                            onClick={() => handleAnswer(q.id, opt.value)}
                                            className={`flex-1 py-3 px-1 rounded-xl text-xs font-bold transition-all duration-200 border-2 ${answers[q.id] === opt.value
                                                ? 'border-blue-600 bg-blue-600 text-white shadow-md scale-[1.02]'
                                                : 'border-gray-100 bg-gray-50 text-gray-400 hover:border-blue-200'
                                                }`}
                                        >
                                            <div className="text-sm mb-0.5">{opt.value}</div>
                                            <span className="hidden sm:inline text-[10px] font-medium opacity-80">{t(likertKeys[opt.value - 1])}</span>
                                        </button>
                                    );
                                })}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* ══════ Navigation ══════ */}
            <div className="flex items-center justify-between gap-3">
                <button
                    onClick={handlePrev}
                    disabled={page === 0}
                    className="flex items-center gap-2 px-5 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-semibold text-sm hover:border-indigo-200 hover:text-indigo-600 transition-all disabled:opacity-30 disabled:cursor-not-allowed"
                >
                    <FiArrowLeft className="w-4 h-4" />
                    {t('back')}
                </button>

                <button
                    onClick={onSkip}
                    className="flex items-center gap-2 px-5 py-3 rounded-xl text-gray-400 font-medium text-sm hover:text-gray-600 transition-all"
                >
                    <FiSkipForward className="w-4 h-4" />
                    {t('cancel')}
                </button>

                {isLastPage ? (
                    <button
                        onClick={handleFinish}
                        disabled={!pageFullyAnswered || isLoading}
                        className="flex items-center gap-2 px-6 py-3 rounded-xl bg-blue-600 text-white font-bold text-sm shadow-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {isLoading ? (
                            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <FiCheck className="w-4 h-4" />
                        )}
                        {t('finish')}
                    </button>
                ) : (
                    <button
                        onClick={handleNext}
                        disabled={!pageFullyAnswered}
                        className="flex items-center gap-2 px-6 py-3 rounded-xl bg-blue-600 text-white font-bold text-sm shadow-lg hover:bg-blue-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {t('next')}
                        <FiArrowRight className="w-4 h-4" />
                    </button>
                )}
            </div>
        </div>
    );
}
