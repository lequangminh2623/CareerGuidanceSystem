'use client';

import { useEffect, useState } from "react";
import Image from "next/image";
import { useTranslation } from "react-i18next";
import { authApis, endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";

interface Student {
    code: string;
}

interface Profile {
    avatar: string;
    lastName: string;
    firstName: string;
    email: string;
    role: string;
    student?: Student;
    createdDate: string;
    updatedDate: string;
    active: boolean;
}

const ProfileClient = () => {
    const [profile, setProfile] = useState<Profile | null>(null);
    const [loading, setLoading] = useState(true);
    const { i18n, t } = useTranslation();


    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await authApis().get(endpoints["profile"]);
                setProfile(res.data);
            } catch (err) {
                console.error("Lỗi khi lấy thông tin profile:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    if (loading) {
        return <MySpinner />;
    }

    if (!profile) {
        return (
            <p className="text-center text-red-600">
                {t('error-message')}
            </p>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <div className="max-w-2xl mx-auto">
                <div className="bg-white rounded-lg shadow-md overflow-hidden">
                    <div className="p-8 text-center">
                        <div className="relative w-[150px] h-[150px] mx-auto mb-4">
                            <Image
                                src={profile.avatar}
                                alt="Avatar"
                                fill
                                className="rounded-full object-cover"
                            />
                        </div>
                        <h4 className="text-2xl font-bold mb-2">
                            {`${profile.lastName} ${profile.firstName}`}
                        </h4>
                        <div className="space-y-2 text-gray-600">
                            <p>{profile.email}</p>
                            {profile.role === "ROLE_STUDENT" && (
                                <p>{t('student-code')}: {profile.student?.code}</p>
                            )}
                            <p>{t('role')}: {profile.role}</p>
                            <p>{t('created-date')}: {profile.createdDate}</p>
                            <p>{t('updated-date')}: {profile.updatedDate}</p>
                            <p>
                                {t('gender')}:{" "}
                                <span>
                                    {profile.active ? t('male') : t('female')}
                                </span>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfileClient;
