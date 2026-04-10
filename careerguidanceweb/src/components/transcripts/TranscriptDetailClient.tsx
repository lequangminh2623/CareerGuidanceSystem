'use client';

import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { authApis, endpoints } from "@/lib/utils/api";
import MySpinner from "@/components/layout/MySpinner";
import {
    FaSave,
    FaPlus,
    FaTimes,
    FaUpload,
    FaLock,
    FaFileCsv,
    FaFilePdf,
} from "react-icons/fa";
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";

// ===== Types =====
export interface ExtraScore {
    id?: number;
    score: number | null;
    scoreIndex: number;
}

export interface ExceptionResponseDTO {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
}

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

interface TranscriptPayload {
    section: SectionResponseDTO;
    scores: ScoreRequestDTO[];
    students: Record<string, UserResponseDTO>;
}

// Internal combined type for rendering
export interface ExceptionResponseDTO {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
}

export interface StudentData {
    studentId: string;
    studentCode: string;
    fullName: string;
    midtermScore: number | null;
    finalScore: number | null;
    extraScores: (number | null)[];
}

interface ScoreErrors {
    [key: string]: {
        mid?: string;
        final?: string;
        extra?: { [key: number]: string };
    };
}

interface Props {
    sectionId: string;
}

const MAX_EXTRA_SCORES = 5; // Allow a bit more flexibility

export default function TranscriptDetailClient({ sectionId }: Props) {
    const [sectionInfo, setSectionInfo] = useState<SectionResponseDTO>({
        id: "",
        classroomId: "",
        teacherName: "",
        classroomName: "",
        gradeName: "",
        yearName: "",
        semesterName: "",
        subjectName: "",
        scoreStatus: "",
    });
    const [students, setStudents] = useState<StudentData[]>([]);
    const [scoreErrors, setScoreErrors] = useState<ScoreErrors>({});
    const [extraCount, setExtraCount] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const q = useSearchParams();
    const displayCount = Math.max(extraCount, 1);
    const { t } = useTranslation();

    // Modal states
    const [confirmDeleteIdx, setConfirmDeleteIdx] = useState<number | null>(null);
    const [confirmLockModal, setConfirmLockModal] = useState<boolean>(false);

    // ===== Fetch transcript =====
    const fetchTranscript = useCallback(async () => {
        try {
            setLoading(true);

            let url = `${endpoints["transcript-details"](sectionId)}`;
            const kw = q.get("kw");
            if (kw) {
                // Attach as query parameter if supported by backend pagination
                url += `?kw=${kw}`;
            }

            const res = await authApis().get<TranscriptPayload>(url);
            const data = res.data;

            setSectionInfo(data.section);

            // Merge user details and scores
            const mergedStudents: StudentData[] = Object.values(data.students).map((user) => {
                const score = data.scores.find((s) => s.studentId === user.id);
                return {
                    studentId: user.id,
                    studentCode: user.code,
                    fullName: `${user.lastName} ${user.firstName}`,
                    midtermScore: score?.midtermScore ?? null,
                    finalScore: score?.finalScore ?? null,
                    extraScores: score && Array.isArray(score.extraScores) ? [...score.extraScores] : [],
                };
            });

            // Re-sort alphabetically by first name and logic
            mergedStudents.sort((a, b) => {
                const nameA = a.fullName.toLowerCase();
                const nameB = b.fullName.toLowerCase();
                return nameA.localeCompare(nameB);
            });

            setStudents(mergedStudents);

            const maxEx = Math.min(
                MAX_EXTRA_SCORES,
                Math.max(0, ...mergedStudents.map((s) => s.extraScores?.length || 0))
            );
            setExtraCount(maxEx);
        } catch (error) {
            const err = error as { response?: { data?: { message?: string } | string }; message?: string };
            console.error(t("error-fetching-transcript"), err);
            const msg = typeof err?.response?.data === 'string' ? err?.response?.data : err?.response?.data?.message;
            alert(msg || err.message || t("error-message"));
        } finally {
            setLoading(false);
        }
    }, [sectionId, q, t]);

    useEffect(() => {
        if (sectionId) {
            fetchTranscript();
        }
    }, [sectionId, q, fetchTranscript]);

    // ===== Extra scores =====
    const addExtra = () => {
        if (extraCount < MAX_EXTRA_SCORES) {
            setExtraCount(extraCount + 1);
            setStudents((prev) =>
                prev.map((s) => ({ ...s, extraScores: [...s.extraScores, null] }))
            );
        }
    };

    const removeExtraAt = (idx: number) => {
        setConfirmDeleteIdx(idx);
    };

    const executeRemoveExtra = () => {
        if (confirmDeleteIdx === null) return;
        const idx = confirmDeleteIdx;

        setExtraCount((prev) => prev - 1);
        setStudents((prev) =>
            prev.map((s) => {
                const arr = [...s.extraScores];
                arr.splice(idx, 1);
                return { ...s, extraScores: arr };
            })
        );
        setConfirmDeleteIdx(null);
    };

    // ===== Change handler =====
    const handleChange = (
        studentId: string,
        field: "mid" | "final" | "extra",
        value: string,
        idx?: number
    ) => {
        setStudents((prev) =>
            prev.map((s) => {
                if (s.studentId !== studentId) return s;
                const val = value === "" ? null : parseFloat(value);
                let error = "";
                if (val !== null && (val < 0 || val > 10)) {
                    error = t("score-range-error");
                }

                setScoreErrors((prevErrs) => {
                    const errs = { ...prevErrs };
                    if (!errs[studentId]) errs[studentId] = {};
                    if (field === "extra" && idx !== undefined) {
                        if (!errs[studentId].extra) errs[studentId].extra = {};
                        errs[studentId].extra[idx] = error;
                    } else {
                        errs[studentId][field] = error;
                    }
                    return errs;
                });

                if (field === "mid") return { ...s, midtermScore: val };
                if (field === "final") return { ...s, finalScore: val };
                if (field === "extra" && idx !== undefined) {
                    const arr = [...s.extraScores];
                    arr[idx] = val;
                    return { ...s, extraScores: arr };
                }
                return s;
            })
        );
    };

    // ===== File upload =====
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const uploadCsv = async () => {
        if (!selectedFile) {
            alert(t("empty-error"));
            return;
        }

        const formData = new FormData();
        formData.append("file", selectedFile);

        try {
            setLoading(true);
            const res = await authApis().post(
                endpoints["transcript-import"](sectionId),
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            alert(res.data);
            fetchTranscript();
        } catch (error) {
            const err = error as { response?: { data?: unknown } };
            if (err.response?.data) {
                const data = err.response.data as ExceptionResponseDTO;
                const message = data.message || (typeof err.response.data === 'string' ? err.response.data : t("error-message"));
                alert(`${t('error')}: ${message}`);
            } else {
                alert(t("error-message"));
            }
        } finally {
            setLoading(false);
        }
    };

    // ===== Save & lock =====
    const saveScores = async () => {
        setLoading(true);
        try {
            // The DTO accepts Double.
            const scoreList = students.map((s) => {
                const cleanExtraScores = s.extraScores.slice(0, extraCount);
                return {
                    studentId: s.studentId,
                    midtermScore: s.midtermScore,
                    finalScore: s.finalScore,
                    extraScores: cleanExtraScores.map(score => (score === null ? null : score)) as number[],
                };
            });

            const res = await authApis().post(
                endpoints["transcript-details"](sectionId),
                { scores: scoreList }
            );
            alert(res.data);
        } catch (error) {
            const err = error as { response?: { data?: unknown } };
            if (err.response?.data) {
                const data = err.response.data as ExceptionResponseDTO;
                const message = data.message || (typeof err.response.data === 'string' ? err.response.data : t("error-message"));
                alert(`${t('error')}: ${message}`);
            } else {
                alert(t("error-message"));
            }
        } finally {
            setLoading(false);
        }
    };

    const lockScores = async () => {
        setConfirmLockModal(false);
        setLoading(true);
        try {
            const res = await authApis().patch(
                endpoints["transcript-lock"](sectionId)
            );
            setSectionInfo((prev) => ({ ...prev, scoreStatus: "Locked" }));
            alert(res.data);
        } catch (error) {
            const err = error as { response?: { data?: unknown } };
            if (err.response?.data) {
                const data = err.response.data as ExceptionResponseDTO;
                const message = data.message || (typeof err.response.data === 'string' ? err.response.data : t("error-message"));
                alert(`${t('error')}: ${message}`);
            } else {
                alert(t("error-message"));
            }
        } finally {
            setLoading(false);
        }
    };

    // ===== Download file =====
    const downloadFile = async (format: "csv" | "pdf") => {
        try {
            const response = await authApis().get(
                endpoints[`export-${format}`](sectionId),
                {
                    responseType: "blob",
                    withCredentials: true,
                }
            );
            if (response.status !== 200) {
                throw new Error(t("error-message"));
            }

            const blob = new Blob([response.data], { type: response.data.type });
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = downloadUrl;
            link.download = `scores_section_${sectionId}.${format}`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(downloadUrl);
        } catch (error) {
            console.error("Download error:", error);
            alert(t("error-message") || "Lỗi tải file");
        }
    };

    // ===== Validation =====
    const hasAnyScoreError = () => {
        for (const sid in scoreErrors) {
            if (!scoreErrors[sid]) continue;
            if (scoreErrors[sid].mid) return true;
            if (scoreErrors[sid].final) return true;
            if (scoreErrors[sid].extra) {
                for (const idx in scoreErrors[sid].extra) {
                    if (scoreErrors[sid].extra[idx]) return true;
                }
            }
        }
        return false;
    };

    if (loading) return <div className="min-h-screen flex items-center justify-center"><MySpinner /></div>;

    // ===== UI =====
    return (
        <>
            <div className="container mx-auto px-4 py-8">
                {/* Header / Info Card */}
                <div className="bg-white p-6 md:p-8 rounded-2xl shadow-sm border border-gray-100 mb-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-64 h-64 bg-linear-to-br from-blue-50 to-indigo-50 rounded-full opacity-50 transform translate-x-1/3 -translate-y-1/3 pointer-events-none"></div>

                    <div className="relative z-10 flex flex-col md:flex-row md:items-start justify-between gap-6 mb-8">
                        <div>
                            <div className="flex items-center gap-3 mb-2">
                                <h2 className="text-3xl font-bold text-gray-900 tracking-tight">
                                    {t('transcript-of-class')} {sectionInfo.classroomName || "..."}
                                </h2>
                                <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shadow-sm border
                                    ${sectionInfo.scoreStatus === 'Locked' ? 'bg-red-50 text-red-600 border-red-200' : 'bg-green-50 text-green-600 border-green-200'}
                                `}>
                                    {sectionInfo.scoreStatus === 'Locked' ? t('locked') : t('unlocked')}
                                </span>
                            </div>
                        </div>

                        <div className="flex flex-wrap gap-2">
                            <button
                                onClick={() => downloadFile("csv")}
                                disabled={sectionInfo.scoreStatus !== "Locked"}
                                className="inline-flex items-center px-4 py-2 border border-gray-200 bg-white text-gray-700 rounded-xl hover:bg-gray-50 hover:border-gray-300 transition shadow-sm disabled:opacity-50 font-medium focus:ring-2 focus:ring-gray-100"
                            >
                                <FaFileCsv className="mr-2 text-green-600" />
                                {t('export-csv')}
                            </button>
                            <button
                                onClick={() => downloadFile("pdf")}
                                disabled={sectionInfo.scoreStatus !== "Locked"}
                                className="inline-flex items-center px-4 py-2 border border-gray-200 bg-white text-gray-700 rounded-xl hover:bg-gray-50 hover:border-gray-300 transition shadow-sm disabled:opacity-50 font-medium focus:ring-2 focus:ring-gray-100"
                            >
                                <FaFilePdf className="mr-2 text-red-500" />
                                {t('export-pdf')}
                            </button>
                        </div>
                    </div>

                    {/* Details Grid */}
                    <div className="relative z-10 grid grid-cols-2 md:grid-cols-5 gap-4 p-5 bg-gray-50/80 rounded-2xl border border-gray-100">
                        <div className="flex flex-col">
                            <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{t('subject')}</span>
                            <span className="text-gray-800 font-semibold">{sectionInfo.subjectName || "—"}</span>
                        </div>
                        <div className="flex flex-col">
                            <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{t('grade') || 'Khối lớp'}</span>
                            <span className="text-gray-800 font-semibold">{sectionInfo.gradeName || "—"}</span>
                        </div>
                        <div className="flex flex-col">
                            <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{t('semester')}</span>
                            <span className="text-gray-800 font-semibold">{sectionInfo.semesterName || "—"}</span>
                        </div>
                        <div className="flex flex-col">
                            <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{t('year')}</span>
                            <span className="text-gray-800 font-semibold">{sectionInfo.yearName || "—"}</span>
                        </div>
                        <div className="flex flex-col">
                            <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{t('teacher-in-charge')}</span>
                            <span className="text-gray-800 font-semibold">{sectionInfo.teacherName || t('not-assigned')}</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white p-6 md:p-8 rounded-2xl shadow-sm border border-gray-100 mb-8">
                    {/* Action Bar */}
                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-6">
                        <div className="flex-1">
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                <FaUpload className="inline mr-2 text-gray-400" /> {t('upload-from-csv')}
                            </label>
                            <div className="flex flex-wrap md:flex-nowrap gap-3">
                                <input
                                    type="file"
                                    accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                    onChange={handleFileChange}
                                    disabled={loading || sectionInfo.scoreStatus === "Locked"}
                                    className="w-full md:max-w-md bg-gray-50 px-4 text-gray-400 py-2 border border-gray-200 rounded-xl file:mr-4 file:py-2 file:px-4
                                                file:rounded-full file:border-0 file:text-sm file:font-medium disabled:opacity-50
                                                file:bg-blue-600 file:text-white hover:file:bg-blue-700 disabled:bg-gray-100 transition focus:outline-none focus:border-blue-300"
                                />
                                <button
                                    onClick={uploadCsv}
                                    disabled={loading || sectionInfo.scoreStatus === "Locked"}
                                    className="inline-flex items-center justify-center px-6 py-2 my-2 bg-blue-600 text-white font-medium rounded-xl shadow-sm hover:bg-blue-700 disabled:opacity-50 transition"
                                >
                                    {t('upload-file')}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="overflow-x-auto rounded-xl border border-gray-200">
                        <div className={loading || sectionInfo.scoreStatus === "Locked" ? "opacity-75 pointer-events-none" : ""}>
                            <table className="min-w-full bg-white text-sm">
                                <thead className="bg-gray-50 border-b border-gray-200 text-gray-700">
                                    <tr>
                                        <th rowSpan={2} className="px-4 py-3 font-semibold text-left border-r border-gray-200">
                                            {t('student-code-short')}
                                        </th>
                                        <th rowSpan={2} className="px-4 py-3 font-semibold text-left border-r border-gray-200">
                                            {t('full-name')}
                                        </th>
                                        <th colSpan={displayCount} className="px-4 py-3 text-center font-semibold border-r border-gray-200">
                                            {t('extra-scores')}{" "}
                                            <button
                                                type="button"
                                                onClick={addExtra}
                                                disabled={extraCount >= MAX_EXTRA_SCORES}
                                                className="ml-2 inline-flex items-center px-2 py-1 text-xs border border-blue-500 text-blue-500 rounded bg-white hover:bg-blue-50 disabled:opacity-50 transition shadow-sm"
                                            >
                                                <FaPlus />
                                            </button>
                                        </th>
                                        <th rowSpan={2} className="px-4 py-3 font-semibold text-center border-r border-gray-200">
                                            {t('midterm-score')}
                                        </th>
                                        <th rowSpan={2} className="px-4 py-3 font-semibold text-center">
                                            {t('final-score')}
                                        </th>
                                    </tr>
                                    <tr className="bg-gray-50 border-b border-gray-200">
                                        {extraCount > 0 ? (
                                            Array.from({ length: extraCount }).map((_, idx) => (
                                                <th key={idx} className="px-3 py-2 text-center border-r border-gray-200 font-medium">
                                                    {t('score-column')} {idx + 1}
                                                    <button
                                                        type="button"
                                                        onClick={(e) => {
                                                            e.preventDefault();
                                                            e.stopPropagation();
                                                            removeExtraAt(idx);
                                                        }}
                                                        className="ml-2 inline-flex items-center px-1.5 py-1 text-[10px] border border-red-500 text-red-500 rounded bg-white hover:bg-red-50 transition"
                                                        title={t("delete")}
                                                    >
                                                        <FaTimes />
                                                    </button>
                                                </th>
                                            ))
                                        ) : (
                                            <th className="px-3 py-2 text-center border-r border-gray-200 text-gray-400 italic font-normal text-xs">
                                                {t('no-score-columns')}
                                            </th>
                                        )}
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-100">
                                    {students.map((s, index) => (
                                        <tr key={s.studentId} className={`hover:bg-blue-50/50 transition ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50/30'}`}>
                                            <td className="px-4 py-3 text-gray-700 border-r border-gray-100 font-medium">{s.studentCode}</td>
                                            <td className="px-4 py-3 text-gray-700 border-r border-gray-100">{s.fullName}</td>

                                            {extraCount > 0 ? (
                                                Array.from({ length: extraCount }).map((_, idx) => (
                                                    <td key={idx} className="px-3 py-2 border-r border-gray-100 align-top">
                                                        <input
                                                            type="number"
                                                            value={s.extraScores[idx] ?? ""}
                                                            onChange={(e) => handleChange(s.studentId, "extra", e.target.value, idx)}
                                                            min={0}
                                                            max={10}
                                                            step={0.1}
                                                            className={`w-full min-w-[60px] max-w-[80px] px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors text-gray-900
                                                                ${scoreErrors[s.studentId]?.extra?.[idx] ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`}
                                                        />
                                                        {scoreErrors[s.studentId]?.extra?.[idx] && (
                                                            <div className="text-red-500 text-[10px] mt-1 text-center font-medium leading-tight">
                                                                {scoreErrors[s.studentId].extra![idx]}
                                                            </div>
                                                        )}
                                                    </td>
                                                ))
                                            ) : (
                                                <td className="px-3 py-2 border-r border-gray-100 bg-gray-50/20 text-center">
                                                    <span className="text-gray-300">-</span>
                                                </td>
                                            )}

                                            <td className="px-3 py-2 border-r border-gray-100 align-top">
                                                <input
                                                    type="number"
                                                    value={s.midtermScore ?? ""}
                                                    onChange={(e) =>
                                                        handleChange(s.studentId, "mid", e.target.value)
                                                    }
                                                    min={0}
                                                    max={10}
                                                    step={0.1}
                                                    className={`w-full min-w-[70px] max-w-[90px] mx-auto block px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors text-gray-900
                                                        ${scoreErrors[s.studentId]?.mid ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`}
                                                />
                                                {scoreErrors[s.studentId]?.mid && (
                                                    <div className="text-red-500 text-[10px] mt-1 text-center font-medium leading-tight">
                                                        {scoreErrors[s.studentId].mid}
                                                    </div>
                                                )}
                                            </td>
                                            <td className="px-3 py-2 align-top">
                                                <input
                                                    type="number"
                                                    value={s.finalScore ?? ""}
                                                    onChange={(e) =>
                                                        handleChange(s.studentId, "final", e.target.value)
                                                    }
                                                    min={0}
                                                    max={10}
                                                    step={0.1}
                                                    className={`w-full min-w-[70px] max-w-[90px] mx-auto block px-2 py-1.5 text-center border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors text-gray-900
                                                        ${scoreErrors[s.studentId]?.final ? 'border-red-500 bg-red-50' : 'border-gray-300 bg-white'}`}
                                                />
                                                {scoreErrors[s.studentId]?.final && (
                                                    <div className="text-red-500 text-[10px] mt-1 text-center font-medium leading-tight">
                                                        {scoreErrors[s.studentId].final}
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                    {students.length === 0 && !loading && (
                                        <tr>
                                            <td colSpan={4 + displayCount} className="px-4 py-8 text-center text-gray-500 italic">
                                                {t('no-students-in-class')}
                                            </td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div className="flex flex-col sm:flex-row justify-between items-center gap-4 mt-6">
                        <p className="text-sm text-gray-500">
                            {t('score-validation-hint')}
                        </p>
                        <div className="flex gap-3 w-full sm:w-auto">
                            <button
                                onClick={saveScores}
                                disabled={
                                    loading || sectionInfo.scoreStatus === "Locked" || hasAnyScoreError()
                                }
                                className="flex-1 sm:flex-none inline-flex justify-center items-center px-6 py-2.5 bg-blue-600 text-white font-medium rounded-lg shadow-sm hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            >
                                <FaSave className="mr-2" />
                                {capitalizeFirstWord(`${t("save")} ${t("scores-table")}`)}
                            </button>

                            <button
                                onClick={() => setConfirmLockModal(true)}
                                disabled={loading || sectionInfo.scoreStatus === "Locked"}
                                className="flex-1 sm:flex-none inline-flex justify-center items-center px-6 py-2.5 bg-red-600 text-white font-medium rounded-lg shadow-sm hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            >
                                <FaLock className="mr-2" />
                                {capitalizeFirstWord(`${t("lock")} ${t("scores-table")}`)}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Custom Modal Delete Column */}
            {confirmDeleteIdx !== null && (
                <div className="fixed inset-0 z-100 flex items-center justify-center bg-black/40 backdrop-blur-sm transition-opacity">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 transform transition-all scale-100 mx-4">
                        <div className="flex items-center gap-3 mb-3 text-red-600">
                            <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                                <FaTimes className="text-xl" />
                            </div>
                            <h3 className="text-lg font-bold text-gray-900">{t('delete-column')}</h3>
                        </div>
                        <p className="text-sm text-gray-600 mb-6 font-medium">{t('confirm-delete-column')}</p>
                        <div className="flex justify-end gap-3 mt-4">
                            <button onClick={() => setConfirmDeleteIdx(null)} className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-800 font-semibold rounded-xl transition-colors">
                                {t('cancel-action')}
                            </button>
                            <button onClick={executeRemoveExtra} className="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white font-semibold rounded-xl shadow-sm transition-colors">
                                {t('delete-confirm')}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Custom Modal Lock Transcript */}
            {confirmLockModal && (
                <div className="fixed inset-0 z-100 flex items-center justify-center bg-black/40 backdrop-blur-sm transition-opacity">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 transform transition-all scale-100 mx-4">
                        <div className="flex items-center gap-3 mb-3 text-red-600">
                            <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                                <FaLock className="text-lg" />
                            </div>
                            <h3 className="text-lg font-bold text-gray-900">{t('lock-transcript-title')}</h3>
                        </div>
                        <p className="text-sm text-gray-600 mb-6 font-medium leading-relaxed">
                            {t('lock-transcript-confirm', { subject: sectionInfo.subjectName })}
                        </p>
                        <div className="flex justify-end gap-3 mt-4">
                            <button onClick={() => setConfirmLockModal(false)} className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-800 font-semibold rounded-xl transition-colors">
                                {t('back')}
                            </button>
                            <button onClick={lockScores} className="px-5 py-2.5 bg-red-600 hover:bg-red-700 text-white font-semibold rounded-xl shadow-sm transition-colors">
                                {t('lock-confirm')}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
