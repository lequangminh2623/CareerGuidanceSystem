import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import UserProvider from "@/components/providers/UserProvider";
import GoogleProvider from "@/components/GoogleLogin/GoogleProvider";

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

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-gray-50 flex flex-col min-h-screen`}
      >
        <GoogleProvider>
          <UserProvider>
            <Header />
            <main className="flex-grow bg-white" style={{ marginTop: "4rem" }}>
              {children}
            </main>
            <Footer />
          </UserProvider>
        </GoogleProvider>
      </body>
    </html>
  );
}
