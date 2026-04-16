'use client';

import React from 'react';
import { MdOutlineErrorOutline } from "react-icons/md";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    icon?: React.ReactNode;
    fullWidth?: boolean;
}

const Input: React.FC<InputProps> = ({
    label,
    error,
    icon,
    fullWidth = true,
    className = '',
    ...props
}) => {
    const widthStyle = fullWidth ? 'w-full' : '';
    
    return (
        <div className={`${widthStyle} ${className}`}>
            {label && (
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                    {label}
                </label>
            )}
            <div className="relative">
                {icon && (
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400">
                        {icon}
                    </div>
                )}
                <input
                    className={`
                        w-full py-3.5 bg-gray-50 border rounded-xl outline-none focus:ring-2 transition-all text-gray-900 placeholder:text-gray-400
                        ${icon ? 'pl-11' : 'px-4'}
                        ${error 
                            ? 'border-red-300 focus:border-red-500 focus:ring-red-500/10' 
                            : 'border-gray-200 focus:border-indigo-500 focus:ring-indigo-500/10'}
                    `}
                    {...props}
                />
            </div>
            {error && (
                <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1 animate-in slide-in-from-left-1">
                    <MdOutlineErrorOutline /> {error}
                </p>
            )}
        </div>
    );
};

export default Input;
