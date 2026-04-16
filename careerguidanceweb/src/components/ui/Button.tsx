'use client';

import React from 'react';
import MySpinner from '../layout/MySpinner';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
    isLoading?: boolean;
    icon?: React.ReactNode;
    fullWidth?: boolean;
}

const Button: React.FC<ButtonProps> = ({
    children,
    variant = 'primary',
    isLoading = false,
    icon,
    fullWidth = false,
    className = '',
    disabled,
    ...props
}) => {
    const baseStyles = 'inline-flex items-center justify-center gap-2 rounded-xl px-6 py-3 text-sm font-bold transition-all active:scale-95 disabled:opacity-50 disabled:active:scale-100 disabled:cursor-not-allowed';
    
    const variants = {
        primary: 'bg-indigo-600 text-white shadow-lg shadow-indigo-200 hover:bg-indigo-700',
        secondary: 'bg-indigo-50 text-indigo-600 hover:bg-indigo-100',
        outline: 'bg-white border-2 border-indigo-600 text-indigo-600 hover:bg-indigo-50',
        danger: 'bg-red-500 text-white shadow-lg shadow-red-200 hover:bg-red-600',
        ghost: 'bg-transparent text-gray-600 hover:bg-gray-100',
    };

    const widthStyle = fullWidth ? 'w-full' : '';

    return (
        <button
            className={`${baseStyles} ${variants[variant]} ${widthStyle} ${className}`}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading && <MySpinner />}
            {!isLoading && icon && <span className="text-lg">{icon}</span>}
            {children}
        </button>
    );
};

export default Button;
