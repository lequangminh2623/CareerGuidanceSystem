'use client';

import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
// Using relative path for robustness
import i18n from '../../lib/i18n';

export default function LocaleProvider({ children }: { children: React.ReactNode }) {
    const { t } = useTranslation(); // Trigger useTranslation hook
    const [isMounted, setIsMounted] = useState(false);

    useEffect(() => {
        let mounted = false;

        const syncLanguage = async () => {
            try {
                const savedLanguage = localStorage.getItem('language');
                if (savedLanguage && savedLanguage !== i18n.language) {
                    await i18n.changeLanguage(savedLanguage);
                }
            } catch (error) {
                console.error("Critical: Language sync failed:", error);
            } finally {
                if (!mounted) {
                    setIsMounted(true);
                    mounted = true;
                }
            }
        };

        // Safety Timeout: Force mount after 1 second no matter what
        const timeoutId = setTimeout(() => {
            if (!mounted) {
                console.warn("LocaleProvider: Safety timeout triggered.");
                setIsMounted(true);
                mounted = true;
            }
        }, 1000);

        syncLanguage();

        return () => {
            mounted = true;
            clearTimeout(timeoutId);
        };
    }, []);

    if (!isMounted) {
        return (
            <div className="fixed inset-0 flex items-center justify-center bg-white z-9999">
                <div className="flex flex-col items-center gap-4">
                    <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-sm font-medium text-gray-400 animate-pulse">Initializing Interface...</p>
                </div>
            </div>
        );
    }

    return <>{children}</>;
}
