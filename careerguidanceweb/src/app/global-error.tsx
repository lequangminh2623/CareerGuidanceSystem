'use client';

import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const { t } = useTranslation();

  useEffect(() => {
    console.error('Fatal global error caught:', error);
  }, [error]);

  return (
    <html lang="vi">
      <body>
        <div className="min-h-screen flex flex-col items-center justify-center p-4 bg-gray-50 text-gray-800">
          <div className="max-w-md w-full bg-white p-8 rounded-2xl shadow-sm border border-gray-100 text-center">
            <h1 className="text-3xl font-bold mb-4 text-red-600">{t('global-error-title')}</h1>
            <p className="text-gray-600 mb-8 whitespace-pre-wrap">
              {t('global-error-desc')}
            </p>
            <button
              onClick={() => reset()}
              className="w-full px-6 py-3 bg-red-600 text-white font-semibold rounded-xl hover:bg-red-700 transition"
            >
              {t('reload-app')}
            </button>
          </div>
        </div>
      </body>
    </html>
  );
}
