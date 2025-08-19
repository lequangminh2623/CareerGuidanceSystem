'use client';

import { useContext } from "react";
import { useRouter } from "next/navigation";
import { MyUserContext } from "@/lib/contexts/userContext";
import { useTranslation } from "react-i18next";

interface Card {
    title: string;
    description: string;
    icon: string;
    href: string;
}

const HomeClient = () => {
    const user = useContext(MyUserContext);
    const router = useRouter();
    const { t } = useTranslation();

    const cards: Card[] = [
        {
            title: t('classrooms'),
            description: t('classrooms-title'),
            icon: '📚',
            href: '/classrooms'
        },
        ...(user?.role === "ROLE_STUDENT" ? [{
            title: t('grades'),
            description: t('grades-title'),
            icon: '💯',
            href: '/grades'
        }] : [{
            title: t('statistics'),
            description: t('statistics-title'),
            icon: '📊',
            href: '/statistics'
        }]),
        {
            title: t('ai-chat'),
            description: t('ai-chat-title'),
            icon: '🤖',
            href: '/chatbox'
        }
    ];

    return (
        <div className="container mx-auto p-3 min-h-screen bg-gray-50">
            <h1 className="text-center mt-1 mb-5 text-2xl font-bold text-gray-800">
                🎓 {t('welcome')}!
            </h1>

            {user?.role !== "ROLE_ADMIN" && (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 justify-items-center">
                    {cards.map((card, index) => (
                        <div
                            key={index}
                            onClick={() => router.push(card.href)}
                            className="w-full max-w-sm p-6 bg-white rounded-lg shadow-sm 
                                     hover:shadow-md transition-all duration-300 cursor-pointer
                                     border border-gray-100"
                        >
                            <div className="text-center">
                                <h2 className="text-xl font-semibold mb-2 text-gray-800">
                                    {card.icon} {card.title}
                                </h2>
                                <p className="text-gray-600">{card.description}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default HomeClient;
