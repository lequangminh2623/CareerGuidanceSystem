'use client';

import Link from "next/link";
import { FaGraduationCap, FaChevronDown, FaGlobe } from "react-icons/fa";
import { useTranslation } from "react-i18next";
import { useState, useRef, useEffect } from "react";
import moment from "moment";

const CompactHeader = () => {
    const { i18n } = useTranslation();
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const changeLanguage = (lng: string): void => {
        i18n.changeLanguage(lng);
        localStorage.setItem('language', lng);
        moment.locale(lng);
        setIsDropdownOpen(false);
    }

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <header className="fixed top-0 w-full z-50 bg-white py-3 shadow-sm border-b border-gray-100">
            <div className="container mx-auto px-4 md:px-6">
                <div className="flex justify-between items-center">
                    {/* Logo */}
                    <Link href="/" className="flex items-center space-x-2 group">
                        <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center shadow-lg group-hover:rotate-12 transition-transform">
                            <span className="text-white font-bold text-lg">
                                <FaGraduationCap />
                            </span>
                        </div>
                        <span className="text-xl font-extrabold bg-clip-text text-transparent bg-linear-to-r from-indigo-600 to-violet-600">
                            Scholar
                        </span>
                    </Link>

                    {/* Language Switcher Only */}
                    <div className="relative" ref={dropdownRef}>
                        <button
                            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                            className="flex items-center space-x-2 px-3 py-1.5 hover:bg-gray-100 rounded-full transition-colors border border-gray-200"
                        >
                            <FaGlobe className="text-gray-500 text-sm" />
                            <span className="text-sm font-medium text-gray-700">
                                {i18n.language === 'vi' ? 'Tiếng Việt' : 'English'}
                            </span>
                            <FaChevronDown className={`text-[10px] text-gray-400 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} />
                        </button>

                        {isDropdownOpen && (
                            <div className="absolute right-0 mt-2 w-40 bg-white rounded-xl shadow-2xl border border-gray-100 py-2 overflow-hidden animate-in fade-in zoom-in duration-200">
                                <button
                                    onClick={() => changeLanguage('vi')}
                                    className={`w-full text-left px-4 py-2 text-sm transition-colors ${i18n.language === 'vi' ? 'bg-indigo-50 text-indigo-600 font-bold' : 'text-gray-600 hover:bg-gray-50'}`}
                                >
                                    Tiếng Việt
                                </button>
                                <button
                                    onClick={() => changeLanguage('en')}
                                    className={`w-full text-left px-4 py-2 text-sm transition-colors ${i18n.language === 'en' ? 'bg-indigo-50 text-indigo-600 font-bold' : 'text-gray-600 hover:bg-gray-50'}`}
                                >
                                    English
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </header>
    );
};

export default CompactHeader;
