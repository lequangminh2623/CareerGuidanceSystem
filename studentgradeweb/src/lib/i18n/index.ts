"use client";

import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import HttpBackend from "i18next-http-backend";

if (!i18n.isInitialized) {
    i18n
        .use(HttpBackend)
        .use(initReactI18next)
        .init({
            react: {
                useSuspense: false,
            },
            lng: typeof window !== "undefined"
                ? localStorage.getItem("language") || "vi"
                : "vi",
            fallbackLng: "vi",
            backend: {
                loadPath: "/locales/{{lng}}/translation.json",
            },
            interpolation: {
                escapeValue: false,
            },
        });
}

export default i18n;
