"use client";

import { GoogleLogin, CredentialResponse } from "@react-oauth/google";
import { useRouter } from "next/navigation";
import { useContext } from "react";
import { MyDispatcherContext } from "@/lib/contexts/userContext";
import { setCookie } from "cookies-next";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import axios from "axios";

// 1. Cập nhật Interface để khớp với LoginForm mới
interface GoogleLoginButtonProps {
  onError: (msg: string) => void;
}

export default function GoogleLoginButton({ onError }: GoogleLoginButtonProps) {
  const dispatch = useContext(MyDispatcherContext);
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
      setCookie("token", res.data.token, {
        maxAge: 60 * 60 * 24 * 7,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax",
      });

      // Lấy Profile từ Backend sau khi đã có Token
      const profileRes = await authApis().get(endpoints["profile"]);

      // Cập nhật Global State
      if (dispatch) {
        dispatch({
          type: "login",
          payload: profileRes.data,
        });
      }

      // Chuyển hướng về trang chủ
      router.push("/");

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
        width="350"
      />
    </div>
  );
}