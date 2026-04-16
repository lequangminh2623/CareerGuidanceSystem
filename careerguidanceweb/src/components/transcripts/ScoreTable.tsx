'use client';

import React from 'react';
import { FaPlus, FaTimes } from "react-icons/fa";
import { useTranslation } from "react-i18next";
import { StudentData } from './TranscriptDetailClient';

interface ScoreTableProps {
    students: StudentData[];
    extraCount: number;
    maxExtra: number;
    scoreErrors: any;
    isLocked: boolean;
    onScoreChange: (studentId: string, field: "mid" | "final" | "extra", value: string, idx?: number) => void;
    onAddExtra: () => void;
    onRemoveExtra: (idx: number) => void;
}

const ScoreTable: React.FC<ScoreTableProps> = ({
    students,
    extraCount,
    maxExtra,
    scoreErrors,
    isLocked,
    onScoreChange,
    onAddExtra,
    onRemoveExtra,
}) => {
    const { t } = useTranslation();
    const displayCount = Math.max(extraCount, 1);

    return (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
            <div className={isLocked ? "opacity-75 pointer-events-none" : ""}>
                <table className="min-w-full bg-white text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200 text-gray-700">
                        <tr>
                            <th rowSpan={2} className="px-4 py-3 font-semibold text-left border-r border-gray-200">{t('student-code-short')}</th>
                            <th rowSpan={2} className="px-4 py-3 font-semibold text-left border-r border-gray-200">{t('full-name')}</th>
                            <th colSpan={displayCount} className="px-4 py-3 text-center font-semibold border-r border-gray-200">
                                {t('extra-scores')}{" "}
                                {!isLocked && (
                                    <button type="button" onClick={onAddExtra} disabled={extraCount >= maxExtra}
                                        className="ml-2 inline-flex items-center px-2 py-1 text-xs border border-blue-500 text-blue-500 rounded bg-white hover:bg-blue-50 disabled:opacity-50 transition shadow-sm">
                                        <FaPlus />
                                    </button>
                                )}
                            </th>
                            <th rowSpan={2} className="px-4 py-3 font-semibold text-center border-r border-gray-200">{t('midterm-score')}</th>
                            <th rowSpan={2} className="px-4 py-3 font-semibold text-center">{t('final-score')}</th>
                        </tr>
                        <tr className="bg-gray-50 border-b border-gray-200">
                            {extraCount > 0 ? (
                                Array.from({ length: extraCount }).map((_, idx) => (
                                    <th key={idx} className="px-3 py-2 text-center border-r border-gray-200 font-medium whitespace-nowrap">
                                        {t('score-column')} {idx + 1}
                                        {!isLocked && (
                                            <button type="button" onClick={(e) => { e.preventDefault(); e.stopPropagation(); onRemoveExtra(idx); }}
                                                className="ml-2 inline-flex items-center px-1.5 py-1 text-[10px] border border-red-500 text-red-500 rounded bg-white hover:bg-red-50 transition">
                                                <FaTimes />
                                            </button>
                                        )}
                                    </th>
                                ))
                            ) : (
                                <th className="px-3 py-2 text-center border-r border-gray-200 text-gray-400 italic font-normal text-xs">{t('no-score-columns')}</th>
                            )}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {students.map((s, index) => (
                            <tr key={s.studentId} className={`hover:bg-blue-50/50 transition ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50/30'}`}>
                                <td className="px-4 py-3 text-gray-700 border-r border-gray-100 font-medium">{s.studentCode}</td>
                                <td className="px-4 py-3 text-gray-700 border-r border-gray-100">{s.fullName}</td>

                                {extraCount > 0 ? (
                                    Array.from({ length: extraCount }).map((_, idx) => (
                                        <td key={idx} className="px-3 py-2 border-r border-gray-100 align-top">
                                            <input 
                                                type="number" 
                                                value={s.extraScores[idx] ?? ""} 
                                                onChange={(e) => onScoreChange(s.studentId, "extra", e.target.value, idx)}
                                                min={0} max={10} step={0.1}
                                                disabled={isLocked}
                                                className={`w-full min-w-[60px] max-w-[80px] px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors text-gray-900 ${scoreErrors[s.studentId]?.extra?.[idx] ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`} 
                                            />
                                            {scoreErrors[s.studentId]?.extra?.[idx] && (
                                                <div className="text-red-500 text-[10px] mt-1 text-center font-medium">{scoreErrors[s.studentId].extra![idx]}</div>
                                            )}
                                        </td>
                                    ))
                                ) : (
                                    <td className="px-3 py-2 border-r border-gray-100 bg-gray-50/20 text-center"><span className="text-gray-300">-</span></td>
                                )}

                                <td className="px-3 py-2 border-r border-gray-100 align-top">
                                    <input 
                                        type="number" 
                                        value={s.midtermScore ?? ""} 
                                        onChange={(e) => onScoreChange(s.studentId, "mid", e.target.value)}
                                        min={0} max={10} step={0.1}
                                        disabled={isLocked}
                                        className={`w-full min-w-[70px] max-w-[90px] mx-auto block px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors text-gray-900 ${scoreErrors[s.studentId]?.mid ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`} 
                                    />
                                    {scoreErrors[s.studentId]?.mid && (
                                        <div className="text-red-500 text-[10px] mt-1 text-center font-medium">{scoreErrors[s.studentId].mid}</div>
                                    )}
                                </td>
                                <td className="px-3 py-2 align-top">
                                    <input 
                                        type="number" 
                                        value={s.finalScore ?? ""} 
                                        onChange={(e) => onScoreChange(s.studentId, "final", e.target.value)}
                                        min={0} max={10} step={0.1}
                                        disabled={isLocked}
                                        className={`w-full min-w-[70px] max-w-[90px] mx-auto block px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors text-gray-900 ${scoreErrors[s.studentId]?.final ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`} 
                                    />
                                    {scoreErrors[s.studentId]?.final && (
                                        <div className="text-red-500 text-[10px] mt-1 text-center font-medium">{scoreErrors[s.studentId].final}</div>
                                    )}
                                </td>
                            </tr>
                        ))}
                        {students.length === 0 && (
                            <tr>
                                <td colSpan={4 + displayCount} className="px-4 py-8 text-center text-gray-500 italic">{t('no-students-in-class')}</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ScoreTable;
