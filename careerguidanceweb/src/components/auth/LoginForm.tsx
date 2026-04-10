'use client';

import { useContext, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { MyDispatcherContext } from "@/lib/contexts/userContext";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import { setCookie } from "cookies-next";
import MySpinner from "@/components/layout/MySpinner";
import GoogleLoginButton from "@/components/GoogleLogin/GoogleLoginButton";
import { FiMail, FiLock } from "react-icons/fi";
import { MdOutlineErrorOutline, MdCheckCircleOutline } from "react-icons/md";
import axios from "axios";
import { useTranslation } from "react-i18next";

// --- Interfaces ---
interface UserData {
    email: string;
    password: string;
}

interface FormField {
    title: string;
    field: keyof UserData;
    type: string;
    icon: React.ReactNode;
}

interface FieldErrors {
    [key: string]: string;
}

interface AlertState {
    type: 'success' | 'error' | '';
    content: string;
}

const LoginForm = () => {
    // 1. States
    const [user, setUser] = useState<Partial<UserData>>({});
    const [alert, setAlert] = useState<AlertState>({ type: '', content: '' });
    const [loading, setLoading] = useState<boolean>(false);
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

    const router = useRouter();
    const searchParams = useSearchParams();
    const dispatch = useContext(MyDispatcherContext);
    const { t } = useTranslation();

    const info: FormField[] = [
        { title: t('email'), field: "email", type: "email", icon: <FiMail className="text-gray-400" /> },
        { title: t('password'), field: "password", type: "password", icon: <FiLock className="text-gray-400" /> }
    ];

    // 2. Logic Helpers
    const handleInputChange = (value: string, field: keyof UserData) => {
        setUser(prev => ({ ...prev, [field]: value }));
        if (fieldErrors[field]) {
            setFieldErrors(prev => {
                const newErrors = { ...prev };
                delete newErrors[field];
                return newErrors;
            });
        }
    };

    const validate = (): boolean => {
        const errors: FieldErrors = {};
        info.forEach(i => {
            if (!user[i.field]) {
                errors[i.field] = i.field === 'email' ? t('email-empty') : t('password-empty');
            }
        });

        if (user.email && !/^[A-Za-z0-9._%+-]+@ou\.edu\.vn$/.test(user.email)) {
            errors.email = t('email-invalid');
        }

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // 3. Main Actions
    const login = async (e: React.FormEvent) => {
        e.preventDefault();
        setAlert({ type: '', content: '' });
        setFieldErrors({});

        if (!validate()) return;

        try {
            setLoading(true);
            const res = await Apis.post(endpoints['login'], user);

            // Lưu token
            setCookie('token', res.data.token);

            // Lấy profile - Đảm bảo authApis sử dụng token mới nhất
            const profileRes = await authApis().get(endpoints['profile']);

            if (dispatch) {
                dispatch({ type: "login", payload: profileRes.data });
            }

            router.push("/");
        } catch (ex: unknown) {
            console.error(t('login-error-log'), ex);

            if (axios.isAxiosError(ex)) {
                const status = ex.response?.status;
                const data = ex.response?.data as { message?: string; details?: { field: string; message: string }[] } | undefined;

                if (status === 400 || status === 401) {
                    // Field validation errors are in `details`
                    if (data?.details && Array.isArray(data.details)) {
                        const errs: FieldErrors = {};
                        data.details.forEach((err) => {
                            errs[err.field] = err.message;
                        });
                        setFieldErrors(errs);
                    } else {
                        setAlert({ type: 'error', content: data?.message || t('login-error') });
                    }
                } else {
                    setAlert({ type: 'error', content: data?.message || t('system-error') });
                }
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (searchParams.get('success') === 'true') {
            setAlert({ type: 'success', content: t('register-success') });
        }
    }, [searchParams, t]);

    return (
        <div className="flex justify-center items-center min-h-screen bg-linear-to-br from-blue-50 to-indigo-100 px-4">
            <div className="w-full max-w-5xl">
                <div className="bg-white/70 backdrop-blur-xl rounded-3xl shadow-2xl overflow-hidden flex flex-col md:row shadow-indigo-200 md:flex-row">

                    {/* Cột trái: Branding */}
                    <div className="md:w-1/2 bg-linear-to-br from-indigo-600 to-blue-500 text-white p-12 flex flex-col justify-center">
                        <h1 className="text-6xl font-extrabold mb-4">{t('app-name')}</h1>
                        <p className="text-xl text-indigo-100 font-light mb-8">
                            {t('branding-subtitle')}
                        </p>
                        <ul className="space-y-4">
                            {[t('feature-1'), t('feature-2'), t('feature-3')].map((text, idx) => (
                                <li key={idx} className="flex items-center gap-3">
                                    <span className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center text-sm">✓</span>
                                    {text}
                                </li>
                            ))}
                        </ul>
                    </div>

                    {/* Cột phải: Form */}
                    <div className="md:w-1/2 bg-white p-10 lg:p-14">
                        <div className="mb-10">
                            <h2 className="text-3xl font-bold text-gray-900">{t('login')}</h2>
                            <p className="text-gray-500">{t('welcome-back')}</p>
                        </div>

                        {/* Alert gộp chung (Dùng chung cho cả lỗi và thành công) */}
                        {alert.content && (
                            <div className={`mb-6 p-4 rounded-xl border flex items-start gap-3 animate-in fade-in slide-in-from-top-2
                                ${alert.type === 'success' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-red-50 text-red-600 border-red-100'}`}>
                                {alert.type === 'success' ? <MdCheckCircleOutline className="w-5 h-5 mt-0.5" /> : <MdOutlineErrorOutline className="w-5 h-5 mt-0.5" />}
                                <span className="text-sm font-medium">{alert.content}</span>
                            </div>
                        )}

                        <form onSubmit={login} className="space-y-6">
                            {info.map((i) => (
                                <div key={i.field}>
                                    <label className="block text-sm font-semibold text-gray-700 mb-2">{i.title}</label>
                                    <div className="relative">
                                        <div className="absolute inset-y-0 left-0 pl-4 flex items-center">{i.icon}</div>
                                        <input
                                            type={i.type}
                                            value={user[i.field] || ""}
                                            onChange={e => handleInputChange(e.target.value, i.field)}
                                            className={`w-full pl-11 pr-4 py-3.5 bg-gray-50 border rounded-xl transition-all outline-none focus:ring-2 text-gray-900 placeholder:text-gray-400
                                                ${fieldErrors[i.field] ? 'border-red-300 focus:ring-red-500/10' : 'border-gray-200 focus:border-indigo-500 focus:ring-indigo-500/10'}`}
                                            placeholder={i.field === 'email' ? t('email-placeholder') : t('password-placeholder')}
                                        />
                                    </div>
                                    {fieldErrors[i.field] && <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1"><MdOutlineErrorOutline /> {fieldErrors[i.field]}</p>}
                                </div>
                            ))}

                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full py-4 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 transition-all disabled:opacity-50"
                            >
                                {loading ? <div className="flex justify-center items-center gap-2"><MySpinner /> {t('authenticating')}</div> : t('login-now')}
                            </button>
                        </form>

                        <div className="mt-8">
                            <div className="relative flex justify-center text-sm mb-6">
                                <span className="px-4 bg-white text-gray-400 relative z-10">{t('or-continue-with')}</span>
                                <div className="absolute top-1/2 w-full border-t border-gray-100"></div>
                            </div>

                            <div className="relative min-h-[44px] flex items-center justify-center">
                                <div className="relative z-10 w-full flex justify-center">
                                    <GoogleLoginButton onError={(m: string) => setAlert({ type: 'error', content: m })} />
                                </div>
                            </div>

                            <p className="mt-8 text-center text-gray-600">
                                {t('no-account')} <Link href="/register" className="text-indigo-600 font-bold hover:underline">{t('register-now')}</Link>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginForm;