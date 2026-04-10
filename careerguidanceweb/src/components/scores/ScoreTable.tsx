'use client';

import { useTranslation } from 'react-i18next';
import { Subject } from './index';

interface SemesterTableProps {
    semesterTitle: string;
    classroomName?: string;
    subjects: Subject[];
}

const calculateSubjectAverage = (subject: Subject): number | null => {
    if (subject.midTermScore === null || subject.finalScore === null) return null;
    const sumTX = subject.extraScore.reduce((sum, score) => sum + score, 0);
    const countTX = subject.extraScore.length;
    return (sumTX + 2 * subject.midTermScore + 3 * subject.finalScore) / (countTX + 5);
};

const calculateSemesterAverage = (subjects: Subject[]): number | null => {
    let total = 0;
    let count = 0;
    for (const s of subjects) {
        const avg = calculateSubjectAverage(s);
        if (avg !== null) {
            total += avg;
            count++;
        }
    }
    return count > 0 ? total / count : null;
};

const SemesterTable = ({ semesterTitle, classroomName, subjects }: SemesterTableProps) => {
    const { t } = useTranslation();
    const semesterAvg = calculateSemesterAverage(subjects);

    const getScoreBadgeColor = (score: number | null) => {
        if (score === null) return 'bg-gray-100 text-gray-500';
        if (score >= 8.5) return 'bg-green-100 text-green-700 font-semibold';
        if (score >= 7.0) return 'bg-blue-100 text-blue-700 font-semibold';
        if (score >= 5.5) return 'bg-yellow-100 text-yellow-700 font-medium';
        if (score >= 4.0) return 'bg-orange-100 text-orange-700 font-medium';
        return 'bg-red-100 text-red-700 font-bold';
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow duration-300">
            {/* Semester Header */}
            <div className="bg-linear-to-r from-blue-600 to-indigo-600 px-6 py-4 flex flex-col md:flex-row md:items-center justify-between gap-2">
                <h3 className="text-white font-bold tracking-wide text-lg">
                    {t('class')}: {classroomName}
                </h3>
                <div className="flex items-center gap-3">
                    <span className="text-white text-sm font-semibold bg-white/20 px-3 py-1 rounded-full border border-white/30 backdrop-blur-sm shadow-sm">
                        {t('semester-avg')}: <span className="ml-1 text-base">{semesterAvg !== null ? semesterAvg.toFixed(2) : '-'}</span>
                    </span>
                    {classroomName && (
                        <span className="text-gray-900 text-sm font-semibold bg-white/70 px-3 py-1 rounded-full w-fit shadow-sm">
                            {semesterTitle}
                        </span>
                    )}
                </div>
            </div>

            <div className="p-0 overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="bg-gray-50 text-gray-600 font-semibold border-b border-gray-100">
                        <tr>
                            <th className="px-6 py-4 whitespace-nowrap">#</th>
                            <th className="px-6 py-4 whitespace-nowrap">{t('subject')}</th>
                            <th className="px-6 py-4 whitespace-nowrap">{t('extra-scores')}</th>
                            <th className="px-6 py-4 whitespace-nowrap">{t('midterm-score')}</th>
                            <th className="px-6 py-4 whitespace-nowrap">{t('final-score')}</th>
                            <th className="px-6 py-4 whitespace-nowrap text-right">{t('subject-avg')}</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {subjects.map((s, idx) => {
                            const subjectAvg = calculateSubjectAverage(s);
                            return (
                                <tr
                                    key={s.id || idx}
                                    className="hover:bg-blue-50/50 transition-colors duration-200 group"
                                >
                                <td className="px-6 py-4 font-medium text-gray-500 group-hover:text-blue-600">
                                    {idx + 1}
                                </td>
                                <td className="px-6 py-4 text-gray-900 font-semibold">
                                    {s.name}
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex flex-wrap gap-1.5">
                                        {s.extraScore.length > 0 ? s.extraScore.map((g, index) => (
                                            <span
                                                key={index}
                                                className="px-2 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-md border border-gray-200"
                                            >
                                                {g.toFixed(1)}
                                            </span>
                                        )) : (
                                            <span className="text-gray-400 italic">-</span>
                                        )}
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center justify-center px-2.5 py-1 rounded-md text-xs ${getScoreBadgeColor(s.midTermScore)}`}>
                                        {s.midTermScore !== null ? s.midTermScore.toFixed(1) : '-'}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center justify-center px-3 py-1.5 rounded-lg text-sm ${getScoreBadgeColor(s.finalScore)} shadow-sm`}>
                                        {s.finalScore !== null ? s.finalScore.toFixed(1) : '-'}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <span className={`inline-flex items-center justify-center px-3 py-1.5 rounded-lg text-sm ${getScoreBadgeColor(subjectAvg)} shadow-sm`}>
                                        {subjectAvg !== null ? subjectAvg.toFixed(1) : '-'}
                                    </span>
                                </td>
                            </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default SemesterTable;
