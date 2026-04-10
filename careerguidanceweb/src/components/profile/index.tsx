'use client';

import { useEffect, useState } from "react";
import Image from "next/image";
import { useTranslation } from "react-i18next";
import { authApis, endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";
import { formatVietnamTime } from "@/lib/utils";
import { FiMail, FiCalendar, FiShield, FiHash, FiUser } from "react-icons/fi";
import { MdOutlineErrorOutline } from "react-icons/md";

// Interface khớp với UserDetailsResponseDTO từ backend (flat structure)
interface Profile {
    id: string;
    firstName: string;
    lastName: string;
    gender: boolean;
    email: string;
    avatar: string;
    createdDate: string;
    updatedDate: string;
    role: string;
    code: string;
    active: boolean;
}

const getRoleLabelMap = (t: (key: string) => string): Record<string, { label: string; color: string; bg: string }> => ({
    Student: { label: t("student"), color: "text-blue-700", bg: "bg-blue-50 border-blue-200" },
    Teacher: { label: t("teacher"), color: "text-emerald-700", bg: "bg-emerald-50 border-emerald-200" },
    Admin: { label: t("admin"), color: "text-purple-700", bg: "bg-purple-50 border-purple-200" },
});

const ProfileClient = () => {
    const [profile, setProfile] = useState<Profile | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const { t } = useTranslation();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await authApis().get(endpoints["profile"]);
                setProfile(res.data);
            } catch (err) {
                console.error(t('error-loading-profile'), err);
                setError(true);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-[60vh]">
                <MySpinner />
            </div>
        );
    }

    if (error || !profile) {
        return (
            <div className="flex justify-center items-center min-h-[60vh]">
                <div className="text-center p-8 bg-red-50 rounded-2xl border border-red-100 max-w-md">
                    <MdOutlineErrorOutline className="w-12 h-12 text-red-400 mx-auto mb-4" />
                    <p className="text-red-600 font-semibold">{t('error-message')}</p>
                    <p className="text-red-400 text-sm mt-2">{t('error-loading-profile')}</p>
                </div>
            </div>
        );
    }

    if (error || !profile) {
        // ... handled above
    }

    const roleLabelMap = getRoleLabelMap(t);
    const roleInfo = roleLabelMap[profile.role] || { label: profile.role, color: "text-gray-700", bg: "bg-gray-50 border-gray-200" };

    return (
        <div className="container mx-auto px-4 py-10">
            <div className="max-w-3xl mx-auto">

                {/* Header Card — Avatar + Name + Role */}
                <div className="bg-white rounded-3xl shadow-lg shadow-gray-200/50 overflow-hidden border border-gray-100">
                    {/* Banner gradient */}
                    <div className="h-36 bg-linear-to-r from-indigo-500 via-blue-500 to-indigo-500 relative">
                        <div className="absolute inset-0 opacity-20">
                            <div className="absolute bottom-4 left-12 w-16 h-16 border border-white/20 rounded-full" />
                        </div>
                    </div>

                    <div className="px-8 pb-8 -mt-16 relative z-10">
                        {/* Avatar */}
                        <div className="flex flex-col sm:flex-row items-center sm:items-end gap-5">
                            <div className="relative w-32 h-32 aspect-square shrink-0 mx-auto sm:mx-0">
                                <div className="w-full h-full rounded-full overflow-hidden border-4 border-white shadow-xl relative">
                                    <Image
                                        src={profile.avatar}
                                        alt="Avatar"
                                        fill
                                        className="object-cover"
                                        priority
                                    />
                                </div>

                                {profile.active && (
                                    <div className="absolute bottom-1 right-1 w-6 h-6 bg-emerald-400 border-[3px] border-white rounded-full shadow-sm z-10" />
                                )}
                            </div>

                            <div className="text-center sm:text-left pb-2 flex-1">
                                <h1 className="text-2xl font-bold text-gray-900">
                                    {`${profile.lastName} ${profile.firstName}`}
                                </h1>
                                <p className="text-gray-700 text-sm mt-1">{profile.email}</p>
                                <div className="mt-3 flex flex-wrap gap-2 justify-center sm:justify-start">
                                    <span className={`inline-flex items-center text-xs font-semibold px-3 py-1 rounded-full border ${roleInfo.bg} ${roleInfo.color}`}>
                                        <FiShield className="w-3 h-3 mr-1.5" />
                                        {roleInfo.label}
                                    </span>
                                    <span className={`inline-flex items-center text-xs font-semibold px-3 py-1 rounded-full border
                                        ${profile.active ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-gray-50 text-gray-500 border-gray-200'}`}>
                                        {profile.active ? t("active") : t("inactive")}
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Info Grid */}
                <div className="mt-6 grid sm:grid-cols-2 gap-4">
                    {/* Gender */}
                    <InfoCard
                        icon={<FiUser className="w-5 h-5 text-indigo-500" />}
                        label={t('gender')}
                        value={profile.gender ? t('male') : t('female')}
                    />

                    {/* Student Code */}
                    {profile.code && (
                        <InfoCard
                            icon={<FiHash className="w-5 h-5 text-blue-500" />}
                            label={t('student-code')}
                            value={profile.code}
                        />
                    )}

                    {/* Email */}
                    <InfoCard
                        icon={<FiMail className="w-5 h-5 text-violet-500" />}
                        label="Email"
                        value={profile.email}
                    />

                    {/* Role */}
                    <InfoCard
                        icon={<FiShield className="w-5 h-5 text-emerald-500" />}
                        label={t('role')}
                        value={roleInfo.label}
                    />

                    {/* Created Date */}
                    <InfoCard
                        icon={<FiCalendar className="w-5 h-5 text-amber-500" />}
                        label={t('created-date')}
                        value={formatVietnamTime(profile.createdDate)}
                    />

                    {/* Updated Date */}
                    <InfoCard
                        icon={<FiCalendar className="w-5 h-5 text-teal-500" />}
                        label={t('updated-date')}
                        value={formatVietnamTime(profile.updatedDate)}
                    />
                </div>
            </div>
        </div>
    );
};

// --- Sub-component ---
interface InfoCardProps {
    icon: React.ReactNode;
    label: string;
    value: string;
}

const InfoCard = ({ icon, label, value }: InfoCardProps) => (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 flex items-start gap-4 hover:shadow-md hover:border-gray-200 transition-all duration-200">
        <div className="p-2.5 bg-gray-50 rounded-xl shrink-0">
            {icon}
        </div>
        <div className="min-w-0">
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wider">{label}</p>
            <p className="text-sm font-semibold text-gray-800 mt-1 truncate">{value}</p>
        </div>
    </div>
);

export default ProfileClient;
