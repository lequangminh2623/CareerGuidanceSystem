"use client";

import { GoogleLogin, CredentialResponse } from "@react-oauth/google";
import { useRouter } from "next/navigation";
import { useAppDispatch } from "@/store/hooks";
import { loginSuccess } from "@/store/features/auth/authSlice";
import { setCookie } from "cookies-next";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import axios from "axios";

// 1. Cập nhật Interface để khớp với LoginForm mới
interface GoogleLoginButtonProps {
  onError: (msg: string) => void;
}

export default function GoogleLoginButton({ onError }: GoogleLoginButtonProps) {
  const dispatch = useAppDispatch();
  const router = useRouter();

  const handleLoginGoogle = async (credentialResponse: CredentialResponse) => {
    // Kiểm tra nếu Google không trả về credential
    if (!credentialResponse.credential) {
      onError("Không lấy được thông tin xác thực từ Google.");
      return;
    }

    try {
      // Gửi token Google sang backend (Spring Boot) để xử lý logic
      const res = await Apis.post(endpoints["login-google"], {
        token: credentialResponse.credential,
      });

      // Trường hợp user mới (chưa có trong DB hoặc thiếu thông tin)
      if (res.data.isNewUser) {
        router.push(
          `/register?newUser=${encodeURIComponent(JSON.stringify(res.data))}`
        );
        return;
      }

      // Lưu token vào Cookie (7 ngày)
      const token = res.data.token as string;
      setCookie("token", token, {
        maxAge: 60 * 60 * 24 * 7,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
      });

      // Truyền token trực tiếp để tránh race condition với getCookie
      const profileRes = await authApis(token).get(endpoints["profile"]);

      dispatch(loginSuccess(profileRes.data));

      // Dùng full page reload để đảm bảo cookie được gửi đúng trong HTTP request
      window.location.href = "/";

    } catch (ex: unknown) {
      console.error("Google Auth Error:", ex);

      if (axios.isAxiosError(ex)) {
        const status = ex.response?.status;
        const data = ex.response?.data as { message?: string } | undefined;

        if (status === 403 || status === 401) {
          onError(data?.message || "Tài khoản Google không được phép truy cập.");
        } else {
          onError(data?.message || "Lỗi xác thực với hệ thống. Vui lòng thử lại sau.");
        }
      } else {
        onError("Đã có lỗi xảy ra trong quá trình kết nối.");
      }
    }
  };

  return (
    <div className="flex justify-center w-full">
      <GoogleLogin
        onSuccess={handleLoginGoogle}
        onError={() => onError("Đăng nhập Google thất bại. Vui lòng kiểm tra lại trình duyệt.")}
        theme="outline"
        shape="pill"
      />
    </div>
  );
}