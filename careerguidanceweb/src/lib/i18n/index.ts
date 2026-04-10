"use client";

import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import viTranslation from "./locales/vi/translation.json";
import enTranslation from "./locales/en/translation.json";

const resources = {
    vi: {
        translation: viTranslation,
    },
    en: {
        translation: enTranslation,
    },
};

if (!i18n.isInitialized) {
    i18n
        .use(initReactI18next)
        .init({
            resources,
            lng: "vi",
            fallbackLng: "vi",
            react: {
                useSuspense: false,
            },
            interpolation: {
                escapeValue: false,
            },
        });
}

export default i18n;
