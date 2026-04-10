import axios, { AxiosInstance } from "axios";
import { getCookie } from "cookies-next";

const BASE_URL = "http://localhost:8080";
const GUIDANCE_API_URL = "http://localhost:8000";

export const endpoints = {
    // Endpoints for user-service
    users: "/user-service/api/secure/users",
    login: "/user-service/api/auth/login",
    "sign-up": "/user-service/api/auth/signup",
    "login-google": "/user-service/api/auth/google",
    profile: "/user-service/api/secure/profile",

    "attendances-classrooms": "/academic-service/api/secure/classrooms/current-student",
    "attendances-by-classroom": (classroomId: string | number) => `/attendance-service/api/secure/classrooms/${classroomId}/attendances`,

    // Transcripts points to score-service
    transcripts: "/academic-service/api/secure/sections",
    "transcript-details": (sectionId: number | string) => `/score-service/api/secure/transcripts/${sectionId}/scores`,
    "transcript-import": (sectionId: number | string) => `/score-service/api/secure/transcripts/${sectionId}/scores/import`,
    "transcript-lock": (sectionId: number | string) => `/academic-service/api/secure/sections/${sectionId}/lock`,
    "export-csv": (sectionId: number | string) => `/score-service/api/secure/transcripts/${sectionId}/scores/export/csv`,
    "export-pdf": (sectionId: number | string) => `/score-service/api/secure/transcripts/${sectionId}/scores/export/pdf`,

    "student-scores": "score-service/api/secure/scores/current-student",
    // Statistics endpoints
    "statistics-student-scores": "/score-service/api/secure/statistics/student",
    "statistics-student-attendance": "/attendance-service/api/secure/statistics/attendance",
    "statistics-teacher-sections": "/score-service/api/secure/statistics/teacher/sections",
    "statistics-teacher-grades": "/score-service/api/secure/statistics/teacher/grades",
    "statistics-subjects": "/score-service/api/secure/statistics/subjects",
};

// ── Career Guidance Python API endpoints ──
export const guidanceEndpoints = {
    hollandGuidance: "/api/holland/guidance",
    academicGuidance: "/api/guidance/academic",
    fullGuidance: "/api/guidance/full",
    springbootGuidance: "/api/guidance/from-springboot",
    chat: "/api/chat",
    chatHistory: (sessionId: string) => `/api/chat/${sessionId}/history`,
    health: "/health",
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

// Axios instance cho Python Guidance API (không cần auth)
export const guidanceApi: AxiosInstance = axios.create({
    baseURL: GUIDANCE_API_URL,
    headers: { "Content-Type": "application/json" },
});
