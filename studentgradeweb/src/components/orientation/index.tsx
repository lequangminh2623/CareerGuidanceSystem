'use client';

import { authApis, endpoints } from '@/lib/utils/api';
import { useState } from 'react';
import { useTranslation } from "react-i18next";

interface OrientationData {
    part_time_job: number;
    extracurricular_activities: number;
    absence_days: number;
    weekly_self_study_hours: number;
}

export default function OrientationForm() {
    const [formData, setFormData] = useState<OrientationData>({
        part_time_job: 0,
        extracurricular_activities: 0,
        absence_days: 0,
        weekly_self_study_hours: 0
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [message, setMessage] = useState<string>('');
    const [result, setResult] = useState<string>('');
    const { t } = useTranslation();

    const handleRadioChange = (name: 'part_time_job' | 'extracurricular_activities', value: number) => {
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name === 'weekly_self_study_hours' ? parseFloat(value) || 0 : parseInt(value) || 0
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setMessage('');
        setResult('');

        try {
            const response = await authApis().post(endpoints["orientate"], formData, {
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.status >= 200 && response.status < 300) {
                const data = response.data;
                setMessage(t("success-message"));
                setResult(data.career_orientation || '');
                setFormData({
                    part_time_job: 0,
                    extracurricular_activities: 0,
                    absence_days: 0,
                    weekly_self_study_hours: 0
                });
            } else {
                setMessage(t("error-message"));
            }
        } catch (error) {
            setMessage(t("error-message"));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-8 p-6 bg-white rounded-lg shadow-md">
            <h1 className="text-2xl font-bold mb-6 text-center">{t("career-orientation")}</h1>

            <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-3">
                        {t("job-question")}
                    </label>
                    <div className="space-y-2">
                        <label className="flex items-center">
                            <input
                                type="radio"
                                name="part_time_job"
                                value="0"
                                checked={formData.part_time_job === 0}
                                onChange={() => handleRadioChange('part_time_job', 0)}
                                className="mr-2"
                            />
                            {t("no")}
                        </label>
                        <label className="flex items-center">
                            <input
                                type="radio"
                                name="part_time_job"
                                value="1"
                                checked={formData.part_time_job === 1}
                                onChange={() => handleRadioChange('part_time_job', 1)}
                                className="mr-2"
                            />
                            {t("yes")}
                        </label>
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-3">
                        {t("extracurricular-question")}
                    </label>
                    <div className="space-y-2">
                        <label className="flex items-center">
                            <input
                                type="radio"
                                name="extracurricular_activities"
                                value="0"
                                checked={formData.extracurricular_activities === 0}
                                onChange={() => handleRadioChange('extracurricular_activities', 0)}
                                className="mr-2"
                            />
                            {t("no")}
                        </label>
                        <label className="flex items-center">
                            <input
                                type="radio"
                                name="extracurricular_activities"
                                value="1"
                                checked={formData.extracurricular_activities === 1}
                                onChange={() => handleRadioChange('extracurricular_activities', 1)}
                                className="mr-2"
                            />
                            {t("yes")}
                        </label>
                    </div>
                </div>

                <div>
                    <label htmlFor="absence_days" className="block text-sm font-medium text-gray-700 mb-1">
                        {t("absence-days-question")}
                    </label>
                    <input
                        type="number"
                        id="absence_days"
                        name="absence_days"
                        value={formData.absence_days}
                        onChange={handleInputChange}
                        min="0"
                        step="1"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Nhập số ngày (ví dụ: 2)"
                    />
                </div>

                <div>
                    <label htmlFor="weekly_self_study_hours" className="block text-sm font-medium text-gray-700 mb-1">
                        {t("self-study-hours-question")}
                    </label>
                    <input
                        type="number"
                        id="weekly_self_study_hours"
                        name="weekly_self_study_hours"
                        value={formData.weekly_self_study_hours}
                        onChange={handleInputChange}
                        min="0"
                        step="0.5"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Nhập số giờ (ví dụ: 10.5)"
                    />
                </div>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-400"
                >
                    {isSubmitting ? '...' : t("send")}
                </button>
            </form>

            {message && (
                <div className={`mt-4 p-3 rounded-md ${message.includes('thành công') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {message}
                </div>
            )}

            {result && (
                <div className="mt-4 p-4 bg-blue-100 border border-blue-200 rounded-md">
                    <h3 className="text-lg font-semibold text-blue-800 mb-2">{t("orientate-result")}:</h3>
                    <p className="text-blue-700 font-medium">{result}</p>
                </div>
            )}
        </div>
    );
}