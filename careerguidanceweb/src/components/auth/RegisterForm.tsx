'use client';

import { useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import Apis, { endpoints } from "@/lib/utils/api";
import GoogleLoginButton from "@/components/GoogleLogin/GoogleLoginButton";
import Image from "next/image";
import { FiUser, FiMail, FiLock, FiHash, FiImage, FiUploadCloud } from "react-icons/fi";
import { MdCheckCircleOutline, MdOutlineErrorOutline } from "react-icons/md";
import axios from "axios";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";

// --- Interfaces ---
interface UserData {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirm: string;
    code: string;
    gender: string;
    [key: string]: string;
}

interface FormField {
    title: string;
    field: keyof UserData;
    type: string;
    disabled: boolean;
    icon?: React.ReactNode;
}

interface FieldErrors {
    [key: string]: string;
}

interface AlertState {
    type: 'success' | 'error' | '';
    content: string;
}

import { useTranslation } from "react-i18next";

const RegisterForm = () => {
    const { t } = useTranslation();
    const router = useRouter();
    const searchParams = useSearchParams();
    const newUserParam = searchParams.get('newUser');
    const isGoogleRegister = !!newUserParam;

    // 1. States
    const [user, setUser] = useState<Partial<UserData>>(() => {
        try {
            return newUserParam ? JSON.parse(newUserParam) : {};
        } catch {
            return {};
        }
    });
    const [alert, setAlert] = useState<AlertState>({ type: '', content: '' });
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [loading, setLoading] = useState<boolean>(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);
    const avatarRef = useRef<HTMLInputElement>(null);

    const info: FormField[] = [
        { title: t("last-name"), field: "lastName", type: "text", disabled: isGoogleRegister, icon: <FiUser className="text-gray-400" /> },
        { title: t("first-name"), field: "firstName", type: "text", disabled: isGoogleRegister, icon: <FiUser className="text-gray-400" /> },
        { title: t("student-code"), field: "code", type: "text", disabled: false, icon: <FiHash className="text-gray-400" /> },
        { title: t("email"), field: "email", type: "email", disabled: isGoogleRegister, icon: <FiMail className="text-gray-400" /> },
        { title: t("password"), field: "password", type: "password", disabled: false, icon: <FiLock className="text-gray-400" /> },
        { title: t("confirm"), field: "confirm", type: "password", disabled: false, icon: <FiLock className="text-gray-400" /> }
    ];

    // 2. Helpers
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

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const url = URL.createObjectURL(file);
            setPreviewImage(url);
            if (fieldErrors.avatar) {
                setFieldErrors(prev => {
                    const newErrs = { ...prev };
                    delete newErrs.avatar;
                    return newErrs;
                });
            }
        }
    };

    const validate = (): boolean => {
        const errors: FieldErrors = {};

        info.forEach(i => {
            if (!user[i.field]?.trim()) {
                errors[i.field] = `${i.title} ${t('empty-error').toLowerCase()}`;
            }
        });

        if (!user.gender) errors.gender = t("select-gender-error");

        if (user.code && user.code.length !== 10) {
            errors.code = t("student-code-length-error");
        }

        if (user.email && !/^[A-Za-z0-9._%+-]+@ou\.edu\.vn$/.test(user.email)) {
            errors.email = t("email-invalid");
        }

        if (!avatarRef.current?.files?.[0] && !previewImage) {
            errors.avatar = t("avatar-empty-error");
        }

        if (user.password !== user.confirm) {
            errors.confirm = t("confirm-password-mismatch");
        }

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // 3. Main Action
    const register = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setAlert({ type: '', content: '' });
        setFieldErrors({});

        if (!validate()) return;

        try {
            setLoading(true);

            const payload = {
                firstName: user.firstName,
                lastName: user.lastName,
                gender: user.gender === "1",
                email: user.email,
                password: user.password,
                code: user.code
            };

            const form = new FormData();
            form.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }));

            if (avatarRef.current?.files?.[0]) {
                form.append("file", avatarRef.current.files[0]);
            }

            await Apis.post(endpoints['sign-up'], form, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            router.push("/login?success=true");
        } catch (ex: unknown) {
            console.error(t('register-error-log'), ex);
            if (axios.isAxiosError(ex)) {
                const data = ex.response?.data as { message?: string; details?: { field: string; message: string }[] } | undefined;

                // Field validation errors are in `details`
                if (data?.details && Array.isArray(data.details)) {
                    const errs: FieldErrors = {};
                    data.details.forEach((err) => {
                        errs[err.field] = err.message;
                    });
                    setFieldErrors(errs);
                } else {
                    setAlert({ type: 'error', content: data?.message || t("system-error") });
                }
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex justify-center items-center min-h-screen bg-linear-to-br from-indigo-50 via-white to-blue-100 py-12 px-4">
            <div className="w-full max-w-4xl">
                <div className="text-center mb-8">
                    <h1 className="text-5xl font-extrabold text-transparent bg-clip-text bg-linear-to-r from-indigo-600 to-blue-500">{t('app-name')}</h1>
                    <p className="mt-3 text-sm text-gray-500 font-bold uppercase tracking-widest">{t('register-portal')}</p>
                </div>

                <div className="bg-white/80 backdrop-blur-xl rounded-3xl shadow-xl border border-white p-8 sm:p-12">
                    <div className="mb-10 border-b border-gray-100 pb-6 text-center md:text-left">
                        <h2 className="text-2xl font-bold text-gray-900">
                            {isGoogleRegister ? t("update-google-account") : t("create-new-account")}
                        </h2>
                    </div>

                    {/* Alert gộp chung */}
                    {alert.content && (
                        <div className={`mb-8 p-4 rounded-2xl border flex items-start gap-3 animate-in fade-in slide-in-from-top-2
                            ${alert.type === 'success' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-red-50 text-red-600 border-red-100'}`}>
                            {alert.type === 'success' ? <MdCheckCircleOutline className="w-5 h-5 mt-0.5" /> : <MdOutlineErrorOutline className="w-5 h-5 mt-0.5" />}
                            <span className="text-sm font-medium">{alert.content}</span>
                        </div>
                    )}

                    <form onSubmit={register} className="space-y-8">
                        <div className="grid md:grid-cols-2 gap-6">
                            {info.map((i) => (
                                <Input
                                    key={i.field}
                                    label={i.title}
                                    type={i.type}
                                    value={user[i.field] || ""}
                                    onChange={e => handleInputChange(e.target.value, i.field)}
                                    disabled={i.disabled}
                                    icon={i.icon}
                                    error={fieldErrors[i.field]}
                                    placeholder={`${t('enter')} ${i.title.toLowerCase()}...`}
                                />
                            ))}

                            <div className="relative group">
                                <label className="block text-sm font-semibold text-gray-700 mb-2">{t('gender')}</label>
                                <select
                                    value={user.gender || ""}
                                    onChange={e => handleInputChange(e.target.value, "gender")}
                                    className={`w-full px-4 py-3.5 bg-gray-50 border text-gray-900 rounded-xl outline-none appearance-none focus:ring-2
                                        ${fieldErrors.gender ? 'border-red-300' : 'border-gray-200 focus:border-indigo-500'}`}
                                >
                                    <option value="" disabled>{t('select-gender')}</option>
                                    <option value="0">{t('female')}</option>
                                    <option value="1">{t('male')}</option>
                                </select>
                                {fieldErrors.gender && <p className="mt-1.5 text-xs text-red-500 flex items-center gap-1"><MdOutlineErrorOutline /> {fieldErrors.gender}</p>}
                            </div>
                        </div>

                        {/* Avatar upload */}
                        <div className="mt-8 bg-gray-50/50 p-6 rounded-2xl border border-gray-100">
                            <label className="block text-sm font-semibold text-gray-700 mb-4">{t('student-avatar')}</label>
                            <div className="flex flex-col sm:flex-row items-center gap-6">
                                <div className="relative w-28 h-28 group cursor-pointer">
                                    <div className={`w-full h-full rounded-full overflow-hidden border-4 transition-all shadow-lg 
                                        ${fieldErrors.avatar ? 'border-red-200' : 'border-white'}`}>
                                        {previewImage ? (
                                            <Image src={previewImage} alt="Preview" fill sizes="112px" className="object-cover" unoptimized />
                                        ) : (
                                            <div className="w-full h-full bg-indigo-50 flex flex-col items-center justify-center text-indigo-300">
                                                <FiImage className="w-8 h-8" />
                                            </div>
                                        )}
                                    </div>
                                    <input
                                        ref={avatarRef}
                                        type="file"
                                        accept="image/*"
                                        className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10"
                                        onChange={handleFileChange}
                                    />
                                    <div className="absolute inset-0 bg-black/20 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                        <FiUploadCloud className="text-white w-6 h-6" />
                                    </div>
                                </div>
                                <div className="text-center sm:text-left">
                                    <p className="text-sm font-bold text-gray-900">{t('upload-image')}</p>
                                    <p className="text-xs text-gray-500 mt-1">{t('upload-image-hint')}</p>
                                    {fieldErrors.avatar && <p className="text-xs text-red-500 mt-2 flex items-center gap-1 justify-center sm:justify-start"><MdOutlineErrorOutline /> {fieldErrors.avatar}</p>}
                                </div>
                            </div>
                        </div>

                        <Button
                            type="submit"
                            isLoading={loading}
                            fullWidth
                        >
                            {t("register-account")}
                        </Button>
                    </form>

                    {!isGoogleRegister && (
                        <div className="mt-8">
                            <p className="mt-8 text-center text-gray-600 text-sm">
                                {t('already-have-account')} <Link href="/login" className="text-indigo-600 font-bold hover:underline">{t('login')}</Link>
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RegisterForm;