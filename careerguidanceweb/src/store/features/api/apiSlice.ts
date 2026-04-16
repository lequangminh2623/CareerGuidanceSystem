import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { getCookie } from 'cookies-next';
import { endpoints, BASE_URL } from '@/lib/utils/api';

/* ─────────────────────── DTOs ─────────────────────── */

export interface ExceptionResponseDTO {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    details?: any;
    path: string;
}

export function getErrorMessage(err: unknown): string {
    if (!err) return "Lỗi không xác định";
    const data = (err as { data?: ExceptionResponseDTO | string })?.data;
    if (typeof data === 'string') return data;
    if (data && typeof data === 'object' && 'message' in data) {
        return data.message || "Lỗi hệ thống";
    }
    // Fallback if err is a direct api slice format
    if (typeof err === 'object' && 'message' in err) {
        return (err as any).message;
    }
    return "Lỗi máy chủ";
}

export interface StudentScoreResponseDTO {
    id: string;
    midtermScore: number | null;
    finalScore: number | null;
    extraScores: number[];
    subjectName: string;
    classroomName: string;
    semesterName: string;
    yearName: string;
}

export interface ProfileDTO {
    id: string;
    firstName: string;
    lastName: string;
    gender: boolean;
    email: string;
    avatar: string;
    createdDate: string;
    updatedDate: string;
    role: string;
    code: string;
    active: boolean;
}

export interface AttendanceStatisticsDTO {
    absentCount: number;
    presentCount?: number;
    lateCount?: number;
}

// Attendances
export interface ClassroomDTO {
    id: string;
    name: string;
}

export interface AttendanceRecordDTO {
    id: string;
    studentId: string;
    attendanceDate: string;
    checkInTime: string;
    status: string;
}

// Statistics — Student
export interface SemesterAvgDTO {
    semesterLabel: string;
    yearName: string;
    semesterName: string;
    avgScore: number;
}

export interface YearAvgDTO {
    yearName: string;
    avgScore: number;
}

export interface StudentStatsDTO {
    semesterAverages: SemesterAvgDTO[];
    yearAverages: YearAvgDTO[];
}

// Statistics — Teacher
export interface SectionAvgDTO {
    sectionLabel: string;
    avgScore: number;
}

export interface GradeSemesterAvgDTO {
    semesterLabel: string;
    avgScore: number;
}

export interface GradeStatisticsDTO {
    gradeName: string;
    semesterAverages: GradeSemesterAvgDTO[];
}

export interface SubjectOptionDTO {
    id: string;
    name: string;
}

// Transcripts
export interface TranscriptDTO {
    id: string;
    classroomId: string;
    teacherName: string;
    classroomName: string;
    gradeName: string;
    semesterName: string;
    yearName: string;
    subjectName: string;
}

export interface TranscriptPageDTO {
    content: TranscriptDTO[];
    totalPages: number;
}

export interface TranscriptListArgs {
    page: number;
    kw?: string;
    sortBy?: string;
}

// Transcript Detail
export interface UserResponseDTO {
    id: string;
    code: string;
    firstName: string;
    lastName: string;
}

export interface ScoreRequestDTO {
    studentId: string;
    midtermScore: number | null;
    finalScore: number | null;
    extraScores: (number | null)[];
}

export interface SectionResponseDTO {
    id: string;
    classroomId: string;
    teacherName: string;
    classroomName: string;
    gradeName: string;
    yearName: string;
    semesterName: string;
    subjectName: string;
    scoreStatus: string;
}

export interface TranscriptDetailDTO {
    section: SectionResponseDTO;
    scores: ScoreRequestDTO[];
    students: Record<string, UserResponseDTO>;
}

export interface SaveScoresArgs {
    sectionId: string;
    scores: ScoreRequestDTO[];
}

export interface ImportCsvArgs {
    sectionId: string;
    formData: FormData;
}

// Chat
export interface ApiUser {
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    avatar: string;
}

export interface UserPageArgs {
    page: number;
    kw?: string;
}

export interface UserPageResponse {
    content: ApiUser[];
    last: boolean;
}

/* ─────────────────────── Base query ─────────────────────── */

const baseQueryWithAuth = fetchBaseQuery({
    baseUrl: BASE_URL,
    prepareHeaders: (headers) => {
        const token = getCookie('token');
        if (token) {
            headers.set('Authorization', `Bearer ${token}`);
        }
        return headers;
    },
});

/* ─────────────────────── API Slice ─────────────────────── */

export const apiSlice = createApi({
    reducerPath: 'api',
    baseQuery: baseQueryWithAuth,
    tagTypes: ['Profile', 'StudentScores', 'Attendance', 'Transcripts', 'Statistics'],
    endpoints: (builder) => ({

        /* ── Profile ── */
        getProfile: builder.query<ProfileDTO, void>({
            query: () => endpoints.profile,
            providesTags: ['Profile'],
        }),

        /* ── Student Scores (list) ── */
        getStudentScores: builder.query<StudentScoreResponseDTO[], void>({
            query: () => endpoints["student-scores"],
            providesTags: ['StudentScores'],
        }),

        /* ── Attendance statistics summary (used in AcademicProfile) ── */
        getAttendanceStatistics: builder.query<AttendanceStatisticsDTO, void>({
            query: () => endpoints["statistics-student-attendance"],
            providesTags: ['Attendance'],
        }),

        /* ── Attendances: classrooms list ── */
        getAttendanceClassrooms: builder.query<ClassroomDTO[], void>({
            query: () => endpoints["attendances-classrooms"],
            providesTags: ['Attendance'],
        }),

        /* ── Attendances: records by classroom ── */
        getAttendancesByClassroom: builder.query<AttendanceRecordDTO[], string>({
            query: (classroomId) => endpoints["attendances-by-classroom"](classroomId),
            providesTags: ['Attendance'],
        }),

        /* ── Statistics: student scores & semester averages ── */
        getStudentStatistics: builder.query<StudentStatsDTO, void>({
            query: () => endpoints["statistics-student-scores"],
            providesTags: ['Statistics'],
        }),

        /* ── Statistics: teacher section averages ── */
        getTeacherSections: builder.query<SectionAvgDTO[], void>({
            query: () => endpoints["statistics-teacher-sections"],
            providesTags: ['Statistics'],
        }),

        /* ── Statistics: subjects list (filter dropdown) ── */
        getSubjects: builder.query<SubjectOptionDTO[], void>({
            query: () => endpoints["statistics-subjects"],
            providesTags: ['Statistics'],
        }),

        /* ── Statistics: teacher grades (optional subjectName filter) ── */
        getTeacherGrades: builder.query<GradeStatisticsDTO[], string | undefined>({
            query: (subjectName?) => {
                const base = endpoints["statistics-teacher-grades"];
                return subjectName
                    ? `${base}?subjectName=${encodeURIComponent(subjectName)}`
                    : base;
            },
            providesTags: ['Statistics'],
        }),

        /* ── Transcripts list (paginated) ── */
        getTranscriptList: builder.query<{ transcripts: TranscriptPageDTO }, TranscriptListArgs>({
            query: ({ page, kw, sortBy }) => {
                let url = `${endpoints.transcripts}?page=${page}`;
                if (kw) url += `&kw=${kw}`;
                if (sortBy) url += `&sortBy=${sortBy}`;
                return url;
            },
            providesTags: ['Transcripts'],
        }),

        /* ── Transcript detail (GET) ── */
        getTranscriptDetail: builder.query<TranscriptDetailDTO, { sectionId: string; kw?: string }>({
            query: ({ sectionId, kw }) => {
                let url = `${endpoints["transcript-details"](sectionId)}`;
                if (kw) url += `?kw=${kw}`;
                return url;
            },
            providesTags: (_res, _err, { sectionId }) => [{ type: 'Transcripts', id: sectionId }],
        }),

        /* ── Transcript: save scores (POST) ── */
        saveScores: builder.mutation<string, SaveScoresArgs>({
            query: ({ sectionId, scores }) => ({
                url: endpoints["transcript-details"](sectionId),
                method: 'POST',
                body: { scores },
                responseHandler: async (response) => {
                    const contentType = response.headers.get('content-type');
                    return contentType?.includes('application/json') ? response.json() : response.text();
                },
            }),
            invalidatesTags: (_res, _err, { sectionId }) => [{ type: 'Transcripts', id: sectionId }],
        }),

        /* ── Transcript: import CSV (POST multipart) ── */
        importScoresCsv: builder.mutation<string, ImportCsvArgs>({
            query: ({ sectionId, formData }) => ({
                url: endpoints["transcript-import"](sectionId),
                method: 'POST',
                body: formData,
                responseHandler: async (response) => {
                    const contentType = response.headers.get('content-type');
                    return contentType?.includes('application/json') ? response.json() : response.text();
                },
            }),
            invalidatesTags: (_res, _err, { sectionId }) => [{ type: 'Transcripts', id: sectionId }],
        }),

        /* ── Transcript: lock scores (PATCH) ── */
        lockScores: builder.mutation<string, string>({
            query: (sectionId) => ({
                url: endpoints["transcript-lock"](sectionId),
                method: 'PATCH',
                responseHandler: async (response) => {
                    const contentType = response.headers.get('content-type');
                    return contentType?.includes('application/json') ? response.json() : response.text();
                },
            }),
            invalidatesTags: (_res, _err, sectionId) => [{ type: 'Transcripts', id: sectionId }],
        }),

        /* ── Chat: users list (paginated + search, infinite scroll) ── */
        getChatUsers: builder.query<UserPageResponse, UserPageArgs>({
            query: ({ page, kw }) => {
                let url = `${endpoints.users}?page=${page}`;
                if (kw?.trim()) url += `&kw=${encodeURIComponent(kw)}`;
                return url;
            },
            // Cache key ignores page so all pages share one cache entry per kw
            serializeQueryArgs: ({ queryArgs }) => queryArgs.kw ?? '',
            // Merge new page results into existing cached list
            merge: (currentCache, newItems, { arg }) => {
                if (arg.page === 1) {
                    // Reset on new search or first page
                    return newItems;
                }
                return {
                    ...newItems,
                    content: [...currentCache.content, ...newItems.content],
                };
            },
            // Always refetch when args change (page or kw)
            forceRefetch: ({ currentArg, previousArg }) =>
                currentArg?.page !== previousArg?.page || currentArg?.kw !== previousArg?.kw,
        }),
    }),
});

export const {
    useGetProfileQuery,
    useGetStudentScoresQuery,
    useGetAttendanceStatisticsQuery,
    useGetAttendanceClassroomsQuery,
    useGetAttendancesByClassroomQuery,
    useGetStudentStatisticsQuery,
    useGetTeacherSectionsQuery,
    useGetSubjectsQuery,
    useGetTeacherGradesQuery,
    useGetTranscriptListQuery,
    useGetTranscriptDetailQuery,
    useSaveScoresMutation,
    useImportScoresCsvMutation,
    useLockScoresMutation,
    useGetChatUsersQuery,
} = apiSlice;
