import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { Suspense } from "react";
import UserProvider from "@/components/providers/UserProvider";
import GoogleProvider from "@/components/GoogleLogin/GoogleProvider";
import RootLayoutWrapper from "@/components/layout/RootLayoutWrapper";
import LocaleProvider from "../components/providers/LocaleProvider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Grade Management System",
  description: "Student Grade Management System",
};

const LoadingFallback = () => (
  <div className="fixed inset-0 flex items-center justify-center bg-white z-9999">
    <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
  </div>
);

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-gray-50 flex flex-col min-h-screen text-gray-700`}
      >
        <GoogleProvider>
          <UserProvider>
            <LocaleProvider>
              <Suspense fallback={<LoadingFallback />}>
                <RootLayoutWrapper>
                  {children}
                </RootLayoutWrapper>
              </Suspense>
            </LocaleProvider>
          </UserProvider>
        </GoogleProvider>
      </body>
    </html>
  );
}