import axios, { AxiosInstance } from "axios";
import { getCookie } from "cookies-next";

const BASE_URL = "http://localhost:8080/api";

export const endpoints = {
    users: "/users",
    login: "/login",
    "login-google": "/auth/google",
    profile: "/secure/profile",

    classrooms: "/secure/classrooms",
    "classroom-details": (classroomId: number | string) => `/secure/classrooms/${classroomId}/grades`,
    "classroom-import": (classroomId: number | string) => `/secure/classrooms/${classroomId}/grades/import`,
    "classroom-lock": (classroomId: number | string) => `/secure/classrooms/${classroomId}/lock`,
    "export-csv": (classroomId: number | string) => `/secure/classrooms/${classroomId}/grades/export/csv`,
    "export-pdf": (classroomId: number | string) => `/secure/classrooms/${classroomId}/grades/export/pdf`,

    "student-grades": "/secure/grades/student",

    ask: "/secure/ai/ask",
    analysis: (semesterId: number | string) => `/secure/ai/analysis/${semesterId}`,

    "forum-posts": (classroomId: number | string) => `/secure/classrooms/${classroomId}/forums`,
    "forum-post-detail": (postId: number | string) => `/secure/forums/${postId}`,
    "forum-reply": (postId: number | string) => `/secure/forums/${postId}/replies`,
    "forum-reply-detail": (postId: number | string, replyId: number | string) =>
        `/secure/forums/${postId}/replies/${replyId}`,
    "forum-child-replies": (postId: number | string, replyId: number | string) =>
        `/secure/forums/${postId}/replies/${replyId}/child-replies`,
};

// Axios instance mặc định (không cần token)
const axiosInstance: AxiosInstance = axios.create({
    baseURL: BASE_URL,
});
export default axiosInstance;

// Axios instance có token (dùng khi cần auth)
export const authApis = (): AxiosInstance => {
    const token = getCookie("token");
    return axios.create({
        baseURL: BASE_URL,
        headers: {
            Authorization: token ? `Bearer ${token}` : "",
        },
    });
};
