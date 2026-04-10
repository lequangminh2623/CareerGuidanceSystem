'use client';

import { usePathname } from "next/navigation";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import CompactHeader from "@/components/layout/CompactHeader";

export default function RootLayoutWrapper({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();
    const isAuthPage = pathname?.startsWith('/login') || pathname?.startsWith('/register');

    return (
        <>
            {isAuthPage ? <CompactHeader /> : <Header />}
            <main 
                className={`grow transition-all duration-300 ${isAuthPage ? 'bg-transparent pt-[60px]' : 'bg-white mt-16'}`}
            >
                {children}
            </main>
            {!isAuthPage && <Footer />}
        </>
    );
}
