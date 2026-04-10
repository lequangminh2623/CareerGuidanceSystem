'use client';

import { useContext, useEffect, useState, useRef } from "react";
import Link from "next/link";
import Image from "next/image";
import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { MyDispatcherContext, MyUserContext } from '@/lib/contexts/userContext';
import { FaBars, FaTimes, FaSearch, FaGlobe, FaSignOutAlt, FaUser, FaChevronDown, FaGraduationCap } from "react-icons/fa";
import { useTranslation } from "react-i18next";
import "@/lib/i18n";
import moment from "moment";
// @ts-ignore
import "moment/locale/vi";

const Header = () => {
    const user = useContext(MyUserContext);
    const dispatch = useContext(MyDispatcherContext);
    const [kw, setKw] = useState<string>("");
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [isScrolled, setIsScrolled] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const mobileMenuRef = useRef<HTMLDivElement>(null);
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const { i18n, t } = useTranslation();


    const changeLanguage = (lng: string): void => {
        i18n.changeLanguage(lng);
        localStorage.setItem('language', lng);
        moment.locale(lng);
        setIsDropdownOpen(false);
    }

    useEffect(() => {
        const handleScroll = () => {
            setIsScrolled(window.scrollY > 10);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);


    useEffect(() => {
        if (isMobileMenuOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }
    }, [isMobileMenuOpen]);

    const search = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();
        const params = new URLSearchParams(searchParams.toString());
        const trimmed = kw.trim();

        if (trimmed) {
            params.set('kw', trimmed);
        } else {
            params.delete('kw');
        }

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        router.push(`${pathname}?${params.toString()}` as any);
        setIsMobileMenuOpen(false);
    };

    const handleLogout = () => {
        setIsDropdownOpen(false);
        setIsMobileMenuOpen(false);
        dispatch?.({ type: 'logout' });
        router.push('/login');
    };

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
            if (mobileMenuRef.current && !mobileMenuRef.current.contains(event.target as Node) && isMobileMenuOpen) {
                // Check if target is the hamburger button to avoid double toggle
                const target = event.target as HTMLElement;
                if (!target.closest('.mobile-menu-button')) {
                    setIsMobileMenuOpen(false);
                }
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isDropdownOpen, isMobileMenuOpen]);


    const navLinks = [
        { href: "/", label: t('home') },
        ...(user && user.role === "Teacher" ? [{ href: "/transcripts", label: t('transcripts') }] : []),
        ...(user && user.role === "Student" ? [{ href: "/scores", label: t('scores') }] : []),
        ...(user && user.role === "Student" ? [{ href: "/attendances", label: t('attendances') }] : []),
        ...(user && user.role !== "Admin" ? [{ href: "/chatbox", label: t('chat') }] : []),
        ...(user && user.role === "Student" ? [{ href: "/orientation", label: t('career-orientation') }] : []),
    ];

    return (
        <header className={`fixed top-0 w-full z-50 transition-all duration-300 ${isScrolled ? 'bg-white/80 backdrop-blur-md shadow-md py-2' : 'bg-white py-4'}`}>
            <div className="container mx-auto px-4 md:px-6">
                <div className="flex justify-between items-center">
                    {/* Logo */}
                    <Link href="/" className="flex items-center space-x-2 group">
                        <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center shadow-lg group-hover:rotate-12 transition-transform">
                            <span className="text-white font-bold text-xl">
                                <FaGraduationCap />
                            </span>
                        </div>
                        <span className="text-2xl font-extrabold bg-clip-text text-transparent bg-linear-to-r from-indigo-600 to-violet-600">
                            Scholar
                        </span>
                    </Link>

                    {/* Desktop Navigation */}
                    <nav className="hidden lg:flex items-center space-x-1">
                        {navLinks.map((link) => (
                            <Link
                                key={link.href}
                                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                                href={link.href as any}
                                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${pathname === link.href ? 'bg-indigo-50 text-indigo-600' : 'text-gray-600 hover:bg-gray-100 hover:text-indigo-600'}`}
                            >
                                {link.label}
                            </Link>
                        ))}
                    </nav>

                    {/* Right Section: Profile */}
                    <div className="hidden lg:flex items-center space-x-4">

                        {user ? (
                            <div className="relative" ref={dropdownRef}>
                                <button
                                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                    className="flex items-center space-x-2 p-1 pl-1 pr-3 hover:bg-gray-100 rounded-full transition-colors border border-transparent hover:border-gray-200"
                                >
                                    <div className="relative">
                                        <Image
                                            src={user.avatar}
                                            alt="User avatar"
                                            width={32}
                                            height={32}
                                            className="rounded-full border border-gray-200"
                                        />
                                        <div className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white rounded-full"></div>
                                    </div>
                                    <span className="text-sm font-semibold text-gray-700 max-w-[100px] truncate">
                                        {user.firstName}
                                    </span>
                                    <FaChevronDown className={`text-[10px] text-gray-400 transition-transform ${isDropdownOpen ? 'rotate-180' : ''}`} />
                                </button>

                                {isDropdownOpen && (
                                    <div className="absolute right-0 mt-3 w-64 bg-white rounded-2xl shadow-2xl border border-gray-100 py-3 overflow-hidden animate-in fade-in zoom-in duration-200">
                                        <div className="px-5 py-3 border-b border-gray-50 mb-2">
                                            <p className="text-xs text-gray-400 font-bold uppercase tracking-wider">{t('account')}</p>
                                            <p className="font-bold text-gray-800 mt-1">{`${user.lastName} ${user.firstName}`}</p>
                                            <p className="text-xs text-indigo-600 font-medium">{user.role}</p>
                                        </div>

                                        <Link href="/profile" onClick={() => setIsDropdownOpen(false)} className="flex items-center space-x-3 px-5 py-2.5 hover:bg-indigo-50 text-gray-600 hover:text-indigo-600 transition-colors">
                                            <FaUser className="text-sm" />
                                            <span className="text-sm font-medium">{t('account')}</span>
                                        </Link>

                                        <div className="px-5 py-2.5">
                                            <div className="flex items-center space-x-3 text-gray-600 mb-2">
                                                <FaGlobe className="text-sm" />
                                                <span className="text-sm font-medium">{t('select_language')}</span>
                                            </div>
                                            <div className="flex space-x-2">
                                                <button
                                                    onClick={() => changeLanguage('vi')}
                                                    className={`flex-1 py-1.5 text-xs rounded-lg border transition-all ${i18n.language === 'vi' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-gray-600 border-gray-200 hover:border-indigo-600'}`}
                                                >
                                                    Tiếng Việt
                                                </button>
                                                <button
                                                    onClick={() => changeLanguage('en')}
                                                    className={`flex-1 py-1.5 text-xs rounded-lg border transition-all ${i18n.language === 'en' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-gray-600 border-gray-200 hover:border-indigo-600'}`}
                                                >
                                                    English
                                                </button>
                                            </div>
                                        </div>

                                        <div className="h-px bg-gray-50 my-2"></div>

                                        <button onClick={handleLogout} className="flex items-center space-x-3 w-full px-5 py-2.5 text-red-500 hover:bg-red-50 transition-colors">
                                            <FaSignOutAlt className="text-sm" />
                                            <span className="text-sm font-bold">{t('logout')}</span>
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                                <Link href="/login" className="px-6 py-2 bg-indigo-600 text-white text-sm font-bold rounded-full hover:bg-indigo-700 shadow-lg shadow-indigo-200 transition-all active:scale-95">
                                    {t('login')}
                                </Link>
                        )}
                    </div>

                    {/* Mobile Menu Toggle */}
                    <button
                        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                        className="lg:hidden p-2 rounded-xl bg-gray-50 text-gray-600 mobile-menu-button active:scale-90 transition-transform"
                    >
                        {isMobileMenuOpen ? <FaTimes size={20} /> : <FaBars size={20} />}
                    </button>
                </div>
            </div>

            {/* Mobile Sidebar Overlay */}
            <div className={`fixed inset-0 bg-black/50 backdrop-blur-sm z-50 lg:hidden transition-opacity duration-300 ${isMobileMenuOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'}`}>
                <div
                    ref={mobileMenuRef}
                    className={`absolute right-0 top-0 h-full w-4/5 max-w-sm bg-white shadow-2xl transition-transform duration-300 transform ${isMobileMenuOpen ? 'translate-x-0' : 'translate-x-full'}`}
                >
                    <div className="flex flex-col h-full">
                        <div className="p-6 flex justify-between items-center border-b border-gray-50">
                            <span className="text-xl font-bold text-gray-800">{t('menu')}</span>
                            <button onClick={() => setIsMobileMenuOpen(false)} className="p-2 text-gray-400 hover:text-gray-600">
                                <FaTimes size={20} />
                            </button>
                        </div>

                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            {/* Search in Mobile */}
                            <form onSubmit={search} className="relative">
                                <input
                                    type="search"
                                    value={kw}
                                    onChange={(e) => setKw(e.target.value)}
                                    placeholder={t('search')}
                                    className="w-full pl-10 pr-4 py-3 bg-gray-50 border border-gray-100 rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white"
                                />
                                <FaSearch className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
                            </form>

                            {/* Nav Links in Mobile */}
                            <nav className="flex flex-col space-y-2">
                                {navLinks.map((link) => (
                                    <Link
                                        key={link.href}
                                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                                        href={link.href as any}
                                        onClick={() => setIsMobileMenuOpen(false)}
                                        className={`px-4 py-3 rounded-xl text-base font-semibold transition-colors ${pathname === link.href ? 'bg-indigo-50 text-indigo-600' : 'text-gray-600 hover:bg-gray-50'}`}
                                    >
                                        {link.label}
                                    </Link>
                                ))}
                            </nav>
                        </div>

                        {/* User Profile in Mobile */}
                        <div className="p-6 border-t border-gray-50 bg-gray-50/50">
                            {user ? (
                                <div className="space-y-4">
                                    <div className="flex items-center space-x-3">
                                        <Image
                                            src={user.avatar}
                                            alt="User avatar"
                                            width={48}
                                            height={48}
                                            className="rounded-full border-2 border-white shadow-sm"
                                        />
                                        <div>
                                            <p className="font-bold text-gray-800">{`${user.lastName} ${user.firstName}`}</p>
                                            <p className="text-xs text-indigo-600 font-medium">{user.role}</p>
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-2">
                                        <button onClick={() => changeLanguage('vi')} className={`py-2 text-xs rounded-lg border font-medium ${i18n.language === 'vi' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-gray-600 border-gray-200'}`}>Tiếng Việt</button>
                                        <button onClick={() => changeLanguage('en')} className={`py-2 text-xs rounded-lg border font-medium ${i18n.language === 'en' ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-gray-600 border-gray-200'}`}>English</button>
                                    </div>
                                    <Link href="/profile" onClick={() => setIsMobileMenuOpen(false)} className="block w-full text-center py-3 bg-white border border-gray-200 rounded-xl text-sm font-bold text-gray-700 hover:bg-gray-50">
                                        {t('account')}
                                    </Link>
                                    <button onClick={handleLogout} className="w-full py-3 bg-red-500 text-white rounded-xl text-sm font-bold shadow-lg shadow-red-200">
                                        {t('logout')}
                                    </button>
                                </div>
                            ) : (
                                <Link href="/login" onClick={() => setIsMobileMenuOpen(false)} className="block w-full text-center py-4 bg-indigo-600 text-white rounded-2xl font-bold shadow-xl shadow-indigo-100 italic transition-transform active:scale-95">
                                    {t('login')}
                                </Link>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;
