'use client';
import { useTranslation } from "react-i18next";

const Footer = () => {
    const { t, ready } = useTranslation();

    if (!ready) return null;

    return (
        <footer className="bg-white shadow-sm text-center text-gray-600 py-3 mt-auto h-[8vh] flex items-center justify-center">
            <div className="container mx-auto px-4">
                <small>&copy; {new Date().getFullYear()} {t('app-name')} </small>
            </div>
        </footer>
    );
};

export default Footer;
