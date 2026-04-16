'use client';

import { useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { hideNotification } from '@/store/features/ui/uiSlice';
import { FiCheckCircle, FiXCircle, FiAlertTriangle, FiInfo, FiX } from 'react-icons/fi';
import { motion, AnimatePresence } from 'framer-motion';

const icons = {
    success: <FiCheckCircle className="w-5 h-5 text-emerald-600" />,
    error: <FiXCircle className="w-5 h-5 text-rose-600" />,
    warning: <FiAlertTriangle className="w-5 h-5 text-amber-600" />,
    info: <FiInfo className="w-5 h-5 text-blue-600" />,
};

const toastColors = {
    success: 'bg-emerald-50 border-emerald-100 text-emerald-900',
    error: 'bg-rose-50 border-rose-100 text-rose-900',
    warning: 'bg-amber-50 border-amber-100 text-amber-900',
    info: 'bg-blue-50 border-blue-100 text-blue-900',
};

const modalIcons = {
    success: <FiCheckCircle className="w-12 h-12 text-emerald-500" />,
    error: <FiXCircle className="w-12 h-12 text-rose-500" />,
    warning: <FiAlertTriangle className="w-12 h-12 text-amber-500" />,
    info: <FiInfo className="w-12 h-12 text-blue-500" />,
};

const modalButtonColors = {
    success: 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-200',
    error: 'bg-rose-600 hover:bg-rose-700 shadow-rose-200',
    warning: 'bg-amber-600 hover:bg-amber-700 shadow-amber-200',
    info: 'bg-blue-600 hover:bg-blue-700 shadow-blue-200',
};

export default function GlobalNotification() {
    const dispatch = useAppDispatch();
    const { isOpen, type, displayMode, title, message, confirmText } = useAppSelector((state) => state.ui);

    useEffect(() => {
        if (isOpen && displayMode === 'toast') {
            const timer = setTimeout(() => {
                dispatch(hideNotification());
            }, 4000);
            return () => clearTimeout(timer);
        }
    }, [isOpen, displayMode, dispatch]);

    const handleClose = () => {
        dispatch(hideNotification());
    };

    return (
        <AnimatePresence>
            {isOpen && (
                <>
                    {displayMode === 'modal' ? (
                        <div className="fixed inset-0 z-1000 flex items-center justify-center p-4">
                            <motion.div
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 1 }}
                                exit={{ opacity: 0 }}
                                onClick={handleClose}
                                className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
                            />
                            <motion.div
                                initial={{ opacity: 0, scale: 0.95, y: 20 }}
                                animate={{ opacity: 1, scale: 1, y: 0 }}
                                exit={{ opacity: 0, scale: 0.95, y: 20 }}
                                className="relative w-full max-w-sm bg-white rounded-3xl shadow-2xl overflow-hidden border border-slate-100 p-8 flex flex-col items-center text-center"
                            >
                                <button onClick={handleClose} className="absolute top-4 right-4 p-2 text-slate-400 hover:text-slate-600 rounded-full">
                                    <FiX className="w-5 h-5" />
                                </button>
                                <div className="mb-6 p-4 rounded-2xl bg-opacity-50">
                                    {modalIcons[type]}
                                </div>
                                <h3 className="text-xl font-bold text-slate-900 mb-2">{title}</h3>
                                <p className="text-slate-500 text-sm mb-8">{message}</p>
                                <button
                                    onClick={handleClose}
                                    className={`w-full py-4 px-6 rounded-2xl text-white font-bold text-sm shadow-lg ${modalButtonColors[type]}`}
                                >
                                    {confirmText}
                                </button>
                            </motion.div>
                        </div>
                    ) : (
                        <div className="fixed top-6 left-0 right-0 z-1000 flex justify-center px-4 pointer-events-none">
                            <motion.div
                                initial={{ opacity: 0, y: -40, scale: 0.9 }}
                                animate={{ opacity: 1, y: 0, scale: 1 }}
                                exit={{ opacity: 0, scale: 0.9, transition: { duration: 0.2 } }}
                                className={`pointer-events-auto flex items-center w-full max-w-2xl px-5 py-4 rounded-2xl border shadow-xl backdrop-blur-md ${toastColors[type]}`}
                            >
                                <div className="mr-4 shrink-0">
                                    {icons[type]}
                                </div>
                                <div className="flex-1 font-bold text-sm tracking-tight pr-4">
                                    {message}
                                </div>
                                <button 
                                    onClick={handleClose}
                                    className="p-1 hover:bg-black/5 rounded-lg transition-colors"
                                >
                                    <FiX className="w-5 h-5 opacity-40 hover:opacity-100" />
                                </button>
                            </motion.div>
                        </div>
                    )}
                </>
            )}
        </AnimatePresence>
    );
}
