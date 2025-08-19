'use client';

import { useContext, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { MyDispatcherContext, MyUserContext } from '@/lib/contexts/userContext';
import { FaRegUser } from "react-icons/fa6";
import { FaSearch } from "react-icons/fa";
import { useTranslation } from "react-i18next";
import "@/lib/i18n";

interface User {
    role: string;
    avatar: string;
    firstName: string;
    lastName: string;
}

const Header = () => {
    const user = useContext(MyUserContext);
    const dispatch = useContext(MyDispatcherContext);
    const [kw, setKw] = useState<string>("");
    const [isOpen, setIsOpen] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const { i18n, t } = useTranslation();

    const changeLanguage = (lng: string): void => {
        i18n.changeLanguage(lng);
        localStorage.setItem('language', lng);
    }

    const search = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();
        const params = new URLSearchParams(searchParams.toString());
        const trimmed = kw.trim();

        if (trimmed) {
            params.set('kw', trimmed);
        } else {
            params.delete('kw');
        }

        router.push(`${pathname}?${params.toString()}`);
    };

    const handleLogout = () => {
        dispatch?.({ type: 'logout' });
        router.push('/login');
    };

    return (
        <nav className="bg-white text-black shadow-sm fixed w-full top-0 z-50">
            <div className="container mx-auto px-4">
                <div className="flex justify-between items-center h-16">
                    <Link href="/" className="text-primary text-2xl font-bold">
                        Grade
                    </Link>

                    <button onClick={() => setIsOpen(!isOpen)} className="xl:hidden">
                        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                d={isOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"} />
                        </svg>
                    </button>

                    <div className={`${isOpen ? 'block' : 'hidden'} xl:flex xl:items-center xl:space-x-8`}>
                        <div className="flex flex-col xl:flex-row xl:space-x-4">
                            <Link href="/" className="px-3 py-2 hover:text-primary">
                                {t('home')}
                            </Link>

                            {(user && user.role !== "ROLE_ADMIN") && <>
                                <Link href="/classrooms" className="px-3 py-2 hover:text-primary">
                                    {t('classrooms')}
                                </Link>

                                {(user.role === "ROLE_STUDENT") &&
                                    <Link href="/grades" className="px-3 py-2 hover:text-primary">
                                        {t('grades')}
                                    </Link>}

                                {(user.role === "ROLE_LECTURER") &&
                                    <Link href="/statistics" className="px-3 py-2 hover:text-primary">
                                        {t('statistics')}
                                    </Link>}

                                <Link href="/chatbox" className="px-3 py-2 hover:text-primary">
                                    {t('chat')}
                                </Link>
                            </>}
                        </div>

                        <form onSubmit={search} className="mt-4 xl:mt-0">
                            <div className="flex">
                                <input
                                    type="search"
                                    value={kw}
                                    onChange={(e) => setKw(e.target.value)}
                                    placeholder={t('search')}
                                    className="px-4 py-2 border rounded-l focus:outline-none focus:ring-2 focus:ring-primary"
                                />
                                <button type="submit"
                                    className="px-4 py-2 bg-primary text-white rounded-r hover:bg-primary-dark">
                                    <FaSearch />
                                </button>
                            </div>
                        </form>

                        {user ? (
                            <div className="relative">
                                <button onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                                    className="flex items-center space-x-2">
                                    <Image
                                        src={user.avatar}
                                        alt="User avatar"
                                        width={40}
                                        height={40}
                                        className="rounded-full"
                                    />
                                </button>

                                {isDropdownOpen && (
                                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg py-2">
                                        <div className="px-4 py-2 text-center">
                                            <Image
                                                src={user.avatar}
                                                alt="User avatar"
                                                width={80}
                                                height={80}
                                                className="rounded-full mx-auto"
                                            />
                                            <p className="mt-2 font-bold">
                                                {`${user.lastName} ${user.firstName}`}
                                            </p>
                                        </div>

                                        <Link href="/profile"
                                            className="block px-4 py-2 hover:bg-gray-100">
                                            {t('account')}
                                        </Link>

                                        <div className="px-4 py-2">
                                            <label className="block text-sm">{t('select_language')}:</label>
                                            <select
                                                value={i18n.language}
                                                onChange={(e) => changeLanguage(e.target.value)}
                                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                                            >
                                                <option value="vi">Tiếng Việt</option>
                                                <option value="en">English</option>
                                            </select>
                                        </div>

                                        <button onClick={handleLogout}
                                            className="block w-full text-left px-4 py-2 text-red-600 hover:bg-gray-100">
                                            {t('logout')}
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <Link href="/login" className="text-gray-700">
                                <FaRegUser size={25} />
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Header;
