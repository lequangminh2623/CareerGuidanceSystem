'use client';

import { useContext, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { MyDispatcherContext } from "@/lib/contexts/userContext";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import { setCookie } from "cookies-next";
import MySpinner from "@/components/layout/MySpinner";
import GoogleLoginButton from "@/components/GoogleLogin/GoogleLoginButton";

interface FormField {
    title: string;
    field: keyof UserData;
    type: string;
}

interface UserData {
    email: string;
    password: string;
}

interface FieldErrors {
    [key: string]: string;
}

const LoginForm = () => {
    const info: FormField[] = [
        { title: "Email", field: "email", type: "email" },
        { title: "Mật khẩu", field: "password", type: "password" }
    ];

    const [user, setUser] = useState<Partial<UserData>>({});
    const [msg, setMsg] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

    const router = useRouter();
    const searchParams = useSearchParams();
    const dispatch = useContext(MyDispatcherContext);

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

        setFieldErrors(errors);
        return isValid;
    };

    const login = async (e: React.FormEvent) => {
        e.preventDefault();
        setMsg('');
        setFieldErrors({});

        if (validate()) {
            try {
                setLoading(true);
                const res = await Apis.post(endpoints['login'], user);

                setCookie('token', res.data.token);
                const u = await authApis().get(endpoints['profile']);

                if (dispatch) {
                    dispatch({
                        type: "login",
                        payload: u.data
                    });
                }

                router.push("/");
            } catch (ex: any) {
                if (ex.response?.status === 401) {
                    setMsg(ex.response.data);
                } else {
                    setMsg("Lỗi hệ thống hoặc kết nối.");
                }
            } finally {
                setLoading(false);
            }
        }
    };

    useEffect(() => {
        const success = searchParams.get('success');
        if (success === 'true') {
            alert("Đăng ký tài khoản thành công");
        }
    }, [searchParams]);

    return (
        <div className="flex text-black justify-center items-center min-h-screen px-4">
            <div className="w-full max-w-6xl">
                <div className="grid md:grid-cols-2 gap-8 items-center">
                    <div>
                        <p className="text-primary text-5xl font-bold">Grade</p>
                        <h4 className="text-xl mt-2">Hệ thống quản lý điểm sinh viên</h4>
                    </div>

                    <div className="bg-white p-8 rounded-2xl shadow-md">
                        <h1 className="text-center text-primary text-2xl font-bold mb-6">
                            Đăng nhập
                        </h1>

                        {msg && (
                            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                                {msg}
                            </div>
                        )}

                        <form onSubmit={login}>
                            {info.map((i) => (
                                <div key={i.field} className="mb-4">
                                    <label className="block text-gray-700 text-sm font-bold mb-2">
                                        {i.title}
                                    </label>
                                    <input
                                        type={i.type}
                                        placeholder={i.title}
                                        value={user[i.field] || ""}
                                        onChange={e => setState(e.target.value, i.field)}
                                        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary
                                            ${fieldErrors[i.field] ? 'border-red-500' : 'border-gray-300'}`}
                                    />
                                    {fieldErrors[i.field] && (
                                        <p className="text-red-500 text-xs mt-1">
                                            {fieldErrors[i.field]}
                                        </p>
                                    )}
                                </div>
                            ))}

                            {loading ? (
                                <div className="mt-6"><MySpinner /></div>
                            ) : (
                                <button type="submit" className="w-full py-3 bg-primary text-white rounded-lg shadow-md hover:bg-blue-700 transition mt-6">
                                    Đăng nhập
                                </button>
                            )}
                        </form>

                        <div className="mt-6 pt-6 border-t border-gray-200">
                            <div className="space-y-4">
                                <GoogleLoginButton setMsg={setMsg} />

                                <p className="text-center text-gray-600">
                                    Chưa có tài khoản?{" "}
                                    <Link href="/register"
                                        className="text-primary hover:underline">
                                        Đăng ký
                                    </Link>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginForm;
