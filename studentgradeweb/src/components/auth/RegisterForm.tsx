'use client';

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import Apis, { endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";
import GoogleLoginButton from "@/components/GoogleLogin/GoogleLoginButton";
import Image from "next/image";

interface FormField {
    title: string;
    field: keyof UserData;
    type: string;
    disabled: boolean;
}

interface UserData {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    confirm: string;
    code: string;
    [key: string]: string;
}

interface FieldErrors {
    [key: string]: string;
}

const RegisterForm = () => {
    const router = useRouter();
    const searchParams = useSearchParams();
    const newUserParam = searchParams.get('newUser');
    const isGoogleRegister = !!newUserParam;
    const [user, setUser] = useState<Partial<UserData>>(newUserParam ? JSON.parse(newUserParam) : {});
    const avatarRef = useRef<HTMLInputElement>(null);
    const [msg, setMsg] = useState<string>("");
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
    const [loading, setLoading] = useState<boolean>(false);
    const [previewImage, setPreviewImage] = useState<string | null>(null);


    const info: FormField[] = [
        {
            title: "Họ và tên lót",
            field: "lastName",
            type: "text",
            disabled: isGoogleRegister
        }, {
            title: "Tên",
            field: "firstName",
            type: "text",
            disabled: isGoogleRegister
        }, {
            title: "Email",
            field: "email",
            type: "email",
            disabled: isGoogleRegister
        }, {
            title: "Giới tính",
            field: "gender",
            type: "select",
            disabled: false
        }, {
            title: "Mã số sinh viên",
            field: "code",
            type: "text",
            disabled: false
        }, {
            title: "Mật khẩu",
            field: "password",
            type: "password",
            disabled: false
        }, {
            title: "Xác nhận mật khẩu",
            field: "confirm",
            type: "password",
            disabled: false
        }
    ];

    const setState = (value: string, field: keyof UserData) => {
        setUser(prev => ({ ...prev, [field]: value }));
    };

    const validate = (): boolean => {
        const errors: FieldErrors = {};
        let isValid = true;

        info.forEach(i => {
            if (!(i.field in user) || user[i.field] === '') {
                errors[i.field] = `${i.title} không được để trống`;
                isValid = false;
            }
        });

        if (!avatarRef.current?.files?.[0]) {
            errors.avatar = "Cần có ảnh đại diện";
            isValid = false;
        }

        if (user.password !== user.confirm) {
            errors.confirm = "Mật khẩu không khớp!";
            isValid = false;
        }

        setFieldErrors(errors);
        return isValid;
    };

    const register = async (e: React.FormEvent) => {
        e.preventDefault();
        setMsg('');
        setFieldErrors({});

        if (validate()) {
            try {
                setLoading(true);
                const form = new FormData();

                Object.entries(user).forEach(([key, value]) => {
                    if (key !== 'confirm' && value) form.append(key, value);
                });

                if (avatarRef.current?.files?.[0]) {
                    form.append("file", avatarRef.current.files[0]);
                }

                await Apis.post(endpoints['users'], form, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });

                router.push("/login?success=true");
            } catch (ex: any) {
                if (ex.response?.status === 400 && Array.isArray(ex.response.data)) {
                    const errs: FieldErrors = {};
                    ex.response.data.forEach((err: { field: string; message: string }) => {
                        errs[err.field] = err.message;
                    });
                    setFieldErrors(errs);
                } else {
                    setMsg("Lỗi hệ thống hoặc kết nối.");
                }
            } finally {
                setLoading(false);
            }
        }
    };

    return (
        <div className="flex justify-center items-center min-h-screen p-4">
            <div className="w-full max-w-4xl">
                <p className="text-primary text-5xl font-bold text-center mb-6">Grade</p>

                <div className="bg-white p-8 rounded-2xl shadow-md">
                    <h1 className="text-center text-primary text-2xl font-bold mb-6">
                        {isGoogleRegister ? "Hoàn tất đăng ký Google" : "Đăng ký tài khoản"}
                    </h1>

                    {msg && (
                        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                            {msg}
                        </div>
                    )}

                    <form onSubmit={register}>
                        <div className="grid md:grid-cols-2 gap-4">
                            {info.map((i) => (
                                <div key={i.field}>
                                    <label className="block text-gray-700 text-sm font-bold mb-2">
                                        {i.title}
                                    </label>
                                    {i.type === "select" ? (
                                        <select value={user[i.field] || ""}
                                            onChange={e => setState(e.target.value, i.field)}
                                            disabled={i.disabled}
                                            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                                ${fieldErrors[i.field] ? 'border-red-500' : 'border-gray-300'}`}
                                        >
                                            <option value="">Chọn giới tính</option>
                                            <option value="0">Nữ</option>
                                            <option value="1">Nam</option>
                                        </select>
                                    ) : (

                                        <input
                                            type={i.type}
                                            placeholder={i.title}
                                            value={user[i.field] || ""}
                                            onChange={e => setState(e.target.value, i.field)}
                                            disabled={i.disabled}
                                            className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                            ${fieldErrors[i.field] ? 'border-red-500' : 'border-gray-300'}`}
                                        />
                                    )}
                                    {fieldErrors[i.field] && (
                                        <p className="text-red-500 text-xs mt-1">{fieldErrors[i.field]}</p>
                                    )}
                                </div>
                            ))}
                        </div>

                        <div className="mt-4 flex gap-4 items-start">
                            <div className="flex-1">
                                <label className="block text-gray-700 text-sm font-bold mb-2">
                                    Avatar
                                </label>
                                <input
                                    ref={avatarRef}
                                    type="file"
                                    accept="image/*"
                                    onChange={(e) => {
                                        const file = e.target.files?.[0];
                                        if (file) {
                                            const url = URL.createObjectURL(file);
                                            setPreviewImage(url);
                                        }
                                    }}
                                    className={`w-full px-3 py-2 border rounded-lg file:mr-4 file:py-2 file:px-4
                                                 file:rounded-full file:border-0 file:text-sm
                                                 file:bg-primary file:text-white hover:file:bg-primary-dark
                                            ${fieldErrors.avatar ? 'border-red-500' : 'border-gray-300'}`}
                                />
                                {fieldErrors.avatar && (
                                    <p className="text-red-500 text-xs mt-1">{fieldErrors.avatar}</p>
                                )}
                            </div>

                            {previewImage && (
                                <div className="flex-shrink-0">
                                    <div className="relative w-32 h-32">
                                        <Image
                                            src={previewImage}
                                            alt="Preview"
                                            fill
                                            className="rounded-full object-cover border-4 border-gray-200"
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        {loading ? (
                            <div className="mt-6"><MySpinner /></div>
                        ) : (
                            <button
                                type="submit"
                                className="w-full bg-primary text-white py-2 rounded-lg hover:bg-primary-dark transition-colors mt-6"
                            >
                                Đăng ký
                            </button>
                        )}
                    </form>

                    {!isGoogleRegister && (
                        <div className="mt-6 pt-6 border-t border-gray-200">
                            <div className="space-y-4">
                                <GoogleLoginButton setMsg={setMsg} />

                                <p className="text-center text-gray-600">
                                    Đã có tài khoản?{" "}
                                    <Link href="/login" className="text-primary hover:underline">
                                        Đăng nhập
                                    </Link>
                                </p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RegisterForm;
