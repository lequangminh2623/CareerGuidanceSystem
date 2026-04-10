/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
    './src/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: '#3b82f6',
        'primary-dark': '#1d4ed8',
        secondary: '#64748b',
        'secondary-dark': '#475569',
        success: '#10b981',
        'success-dark': '#059669',
        warning: '#f59e0b',
        'warning-dark': '#d97706',
        danger: '#ef4444',
        'danger-dark': '#dc2626',
        info: '#06b6d4',
        'info-dark': '#0891b2',
      },
      fontFamily: {
        sans: ['var(--font-geist-sans)', 'system-ui', 'sans-serif'],
        mono: ['var(--font-geist-mono)', 'monospace'],
      },
    },
  },
  plugins: [],
}

