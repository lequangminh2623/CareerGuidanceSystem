"use client";

import { GoogleLogin, CredentialResponse } from "@react-oauth/google";
import { useRouter } from "next/navigation";
import { useContext } from "react";
import { MyDispatcherContext } from "@/lib/contexts/userContext";
import { setCookie } from "cookies-next";
import { jwtDecode } from "jwt-decode";
import Apis, { authApis, endpoints } from "@/lib/utils/api";
import axios from "axios";

interface GoogleLoginButtonProps {
  setMsg: (msg: string) => void;
}

interface GoogleJwtPayload {
  email: string;
  name: string;
  picture: string;
  sub: string;
}

export default function GoogleLoginButton({ setMsg }: GoogleLoginButtonProps) {
  const dispatch = useContext(MyDispatcherContext);
  const router = useRouter();

  const handleLoginGoogle = async (credentialResponse: CredentialResponse) => {
    if (!credentialResponse.credential) {
      setMsg("Không lấy được thông tin đăng nhập từ Google");
      return;
    }

    // Decode JWT từ Google để lấy thông tin cơ bản
    const decoded = jwtDecode<GoogleJwtPayload>(credentialResponse.credential);

    try {
      // Gửi token Google sang backend để xác thực
      const res = await Apis.post(endpoints["login-google"], {
        token: credentialResponse.credential,
      });

      if (res.data.isNewUser) {
        // Nếu là user mới → chuyển sang trang đăng ký
        router.push(
          `/register?newUser=${encodeURIComponent(JSON.stringify(res.data))}`
        );
      } else {

        setCookie("token", res.data.token, {
          maxAge: 60 * 60 * 24 * 7,
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax",
        });

        // Lấy thông tin profile từ backend
        const u = await authApis().get(endpoints["profile"]);

        // Dispatch vào context (global state)
        dispatch?.({
          type: "login",
          payload: u.data,
        });

        // Chuyển về trang chủ
        router.push("/");
      }
    } catch (ex: unknown) {
      if (axios.isAxiosError(ex) && ex.response?.status === 403) {
        setMsg(ex.response.data as string);
      } else {
        setMsg("Lỗi hệ thống hoặc kết nối.");
      }
    }
  };

  return (
    <GoogleLogin
      onSuccess={handleLoginGoogle}
      onError={() => console.log("Login Failed")}
    />
  );
}
