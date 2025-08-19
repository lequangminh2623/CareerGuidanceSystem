'use client';

import { useState } from 'react';
import { useTranslation } from 'react-i18next';

interface Subject {
    code: string;
    classCode: string;
    name: string;
    credit: number;
    extraGrade: number[];
    midTermGrade: number | null;
    finalGrade: number | null;
}

interface Summary {
    gpa: number;
    credits: number;
}

interface SemesterTableProps {
    semesterTitle: string;
    subjects: Subject[];
    summary: Summary | null;
}

const SemesterTable = ({ semesterTitle, subjects, summary }: SemesterTableProps) => {
    const [isHovered, setIsHovered] = useState(false);
    const { t } = useTranslation();

    return (
        <div
            className="mb-4 bg-white rounded-lg shadow-sm overflow-hidden"
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <div className={`${isHovered ? 'bg-primary text-white' : 'bg-gray-50'} 
                transition-colors duration-300 text-center py-3 px-4 font-medium text-lg`}>
                {semesterTitle}
            </div>

            <div className="p-4">
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className={`${isHovered ? 'bg-blue-50' : 'bg-gray-50'} transition-colors duration-300`}>
                            <tr>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">#</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('course-code')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('classrooms')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('course')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('credit')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('extra-grades')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('midterm-grade')}</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">{t('final-grade')}</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {subjects.map((s, idx) => (
                                <tr key={idx} className="hover:bg-gray-50">
                                    <td className="px-4 py-3 whitespace-nowrap">{idx + 1}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.code}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.classCode}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.name}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.credit}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">
                                        {s.extraGrade.length > 0 ? s.extraGrade.map((g, index) => (
                                            <span key={index} className="mr-2">{g}</span>
                                        )) : '-'}
                                    </td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.midTermGrade || '-'}</td>
                                    <td className="px-4 py-3 whitespace-nowrap">{s.finalGrade || '-'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {summary && (
                    <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <strong>Điểm trung bình tích lũy hệ 4:</strong> {summary.gpa}
                        </div>
                        <div>
                            <strong>Số tín chỉ tích lũy:</strong> {summary.credits}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SemesterTable;
