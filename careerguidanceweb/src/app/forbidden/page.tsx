'use client';

import Link from 'next/link';
import { FaLock } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

export default function ForbiddenPage() {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 flex-col px-4 text-center">
      <div className="bg-white p-8 md:p-12 rounded-3xl shadow-sm border border-gray-100 max-w-lg w-full">
        <div className="w-20 h-20 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-6">
          <FaLock className="text-3xl" />
        </div>
        <h1 className="text-4xl font-extrabold text-gray-900 mb-4 tracking-tight">403</h1>
        <h2 className="text-xl font-semibold text-gray-800 mb-4">{t('forbidden-title')}</h2>
        <p className="text-gray-500 mb-8 leading-relaxed">
          {t('forbidden-desc')}
        </p>
        
        <Link 
          href="/" 
          className="inline-flex items-center justify-center px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition duration-200 w-full sm:w-auto shadow-sm"
        >
          {t('back-to-home')}
        </Link>
      </div>
    </div>
  );
}
