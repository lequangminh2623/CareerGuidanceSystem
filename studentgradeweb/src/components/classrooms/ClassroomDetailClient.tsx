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
interface ClassInfo {
    classroomName: string;
    teacherName: string;
    courseName: string;
    academicTerm: string;
    gradeStatus: string;
}

export interface Student {
    studentId: string;
    studentCode: string;
    fullName: string;
    extraGrades: (number | null)[];
    midtermGrade: number | null;
    finalGrade: number | null;
}

interface GradeErrors {
    [key: string]: {
        mid?: string;
        final?: string;
        extra?: { [key: number]: string };
    };
}

interface TranscriptResponse {
    classroomName: string;
    teacherName: string;
    courseName: string;
    academicTerm: string;
    gradeStatus: string;
    students: Student[];
}

interface Props {
    classroomId: string;
}

const MAX_EXTRA_GRADES = 3;

// ===== Component =====
export default function ClassroomDetailClient({ classroomId }: Props) {
    const [classInfo, setClassInfo] = useState<ClassInfo>({
        classroomName: "",
        teacherName: "",
        courseName: "",
        academicTerm: "",
        gradeStatus: "",
    });
    const [students, setStudents] = useState<Student[]>([]);
    const [gradeErrors, setGradeErrors] = useState<GradeErrors>({});
    const [extraCount, setExtraCount] = useState<number>(0);
    const [loading, setLoading] = useState<boolean>(false);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const q = useSearchParams();
    const [page, setPage] = useState<number>(1);
    const displayCount = Math.max(extraCount, 1);
    const { t } = useTranslation();

    // ===== Fetch transcript =====
    const fetchTranscript = useCallback(async () => {
        try {
            setLoading(true);

            let url = `${endpoints["classroom-details"](classroomId)}?page=${page}`;

            const kw = q.get("kw");
            if (kw) {
                url += `&kw=${kw}`;
            }

            const res = await authApis().get<TranscriptResponse>(url);
            const data = res.data;

            setClassInfo({
                classroomName: data.classroomName,
                teacherName: data.teacherName,
                courseName: data.courseName,
                academicTerm: data.academicTerm,
                gradeStatus: data.gradeStatus,
            });

            const studentsCopy: Student[] = data.students.map((s) => ({
                ...s,
                extraGrades: Array.isArray(s.extraGrades) ? [...s.extraGrades] : [],
            }));
            setStudents(studentsCopy);

            const maxEx = Math.min(
                MAX_EXTRA_GRADES,
                Math.max(0, ...studentsCopy.map((s) => s.extraGrades?.length || 0))
            );
            setExtraCount(maxEx);
        } catch (err) {
            console.error("Lỗi khi lấy bảng điểm:", err);
        } finally {
            setLoading(false);
        }
    }, [classroomId, page, q]);

    useEffect(() => {
        if (page > 0) {
            fetchTranscript();
        }
    }, [page, q, fetchTranscript]);

    // ===== Extra grades =====
    const addExtra = () => {
        if (extraCount < MAX_EXTRA_GRADES) {
            setExtraCount(extraCount + 1);
            setStudents((prev) =>
                prev.map((s) => ({ ...s, extraGrades: [...s.extraGrades, null] }))
            );
        }
    };

    const removeExtraAt = (idx: number) => {
        if (idx < 0 || idx >= extraCount) return;

        const confirmDelete = window.confirm(
            t("confirm-remove")
        );
        if (!confirmDelete) return;

        setExtraCount(extraCount - 1);
        setStudents((prev) =>
            prev.map((s) => {
                const arr = [...s.extraGrades];
                arr.splice(idx, 1);
                return { ...s, extraGrades: arr };
            })
        );
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
                    error = t("grade-range-error");
                }

                setGradeErrors((prevErrs) => {
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

                if (field === "mid") return { ...s, midtermGrade: val };
                if (field === "final") return { ...s, finalGrade: val };
                if (field === "extra" && idx !== undefined) {
                    const arr = [...s.extraGrades];
                    arr[idx] = val;
                    return { ...s, extraGrades: arr };
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
            const res = await authApis().post(
                endpoints["classroom-import"](classroomId),
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                    },
                }
            );

            alert(res.data);
            fetchTranscript();
        } catch (err: any) {
            alert(err.response?.data || t("error-message"));
        }
    };

    // ===== Save & lock =====
    const saveGrades = async () => {
        setLoading(true);
        try {
            const payload = students.map((s) => ({
                studentId: s.studentId,
                midtermGrade: s.midtermGrade,
                finalGrade: s.finalGrade,
                extraGrades: s.extraGrades.slice(0, extraCount),
            }));

            const res = await authApis().post(
                endpoints["classroom-details"](classroomId),
                payload
            );
            alert(res.data);
        } catch (err: any) {
            alert(err.response?.data || t("error-message"));
        } finally {
            setLoading(false);
        }
    };

    const lockGrades = async () => {
        const confirmed = window.confirm(
            t("confirm-remove")
        );
        if (!confirmed) return;

        setLoading(true);
        try {
            const res = await authApis().patch(
                endpoints["classroom-lock"](classroomId)
            );
            setClassInfo((prev) => ({ ...prev, gradeStatus: "LOCKED" }));
            alert(res.data);
        } catch (err: any) {
            alert(err.response?.data || t("error-message"));
        } finally {
            setLoading(false);
        }
    };

    // ===== Download file =====
    const downloadFile = async (format: "csv" | "pdf") => {
        try {
            const response = await authApis().get(
                endpoints[`export-${format}`](classroomId),
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
            link.download = `grades_classroom_${classroomId}.${format}`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(downloadUrl);
        } catch (err) {
            console.error("Download error:", err);
            alert(t("error-message"));
        }
    };

    // ===== Validation =====
    const hasAnyGradeError = () => {
        for (const sid in gradeErrors) {
            if (!gradeErrors[sid]) continue;
            if (gradeErrors[sid].mid) return true;
            if (gradeErrors[sid].final) return true;
            if (gradeErrors[sid].extra) {
                for (const idx in gradeErrors[sid].extra) {
                    if (gradeErrors[sid].extra[idx]) return true;
                }
            }
        }
        return false;
    };

    if (loading) return <MySpinner />;

    // ===== UI =====
    return (
        <div className="container mx-auto px-4 py-6 text-gray-700">
            <div className="flex justify-between items-center flex-wrap mb-6">
                <h3 className="text-2xl font-bold text-gray-700">
                    {capitalizeFirstWord(`${t("grades-table")} ${t("classrooms")}`)}
                </h3>

                <div className="flex gap-2">
                    <button
                        onClick={() => downloadFile("csv")}
                        disabled={classInfo.gradeStatus !== "LOCKED"}
                        className="inline-flex items-center px-4 py-2 border border-green-500 text-green-500 rounded-lg hover:bg-green-50 disabled:opacity-50"
                    >
                        <FaFileCsv className="mr-2" />
                        {t("export")} CSV
                    </button>

                    <button
                        onClick={() => downloadFile("pdf")}
                        disabled={classInfo.gradeStatus !== "LOCKED"}
                        className="inline-flex items-center px-4 py-2 border border-red-500 text-red-500 rounded-lg hover:bg-red-50 disabled:opacity-50"
                    >
                        <FaFilePdf className="mr-2" />
                        {t("export")} PDF
                    </button>
                </div>
            </div>

            <div className="bg-gray-50 p-6 rounded-lg shadow-sm mb-6">
                <div className="grid md:grid-cols-2 gap-6">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t("classrooms")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.classroomName}
                            readOnly
                            className="w-full px-3 py-2 bg-gray-100 border rounded-lg"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t("teacher")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.teacherName}
                            readOnly
                            className="w-full px-3 py-2 bg-gray-100 border rounded-lg"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t("course")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.courseName}
                            readOnly
                            className="w-full px-3 py-2 bg-gray-100 border rounded-lg"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            {t("semester")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.academicTerm}
                            readOnly
                            className="w-full px-3 py-2 bg-gray-100 border rounded-lg"
                        />
                    </div>
                </div>

                <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-700 mb-2 mt-6">
                        {t("upload")} {t("grades-table")} CSV:
                    </label>
                    <div className="flex gap-2">
                        <input
                            type="file"
                            accept=".csv"
                            onChange={handleFileChange}
                            disabled={loading || classInfo.gradeStatus === "LOCKED"}
                            className="w-full bg-white px-3 py-2 border rounded-lg file:mr-4 file:py-2 file:px-4
                                         file:rounded-full file:border-0 file:text-sm disabled:opacity-50
                                         file:bg-primary file:text-white hover:file:bg-primary-dark disabled:bg-gray-200"
                        />
                        <button
                            onClick={uploadCsv}
                            disabled={loading || classInfo.gradeStatus === "LOCKED"}
                            className="min-w-[110px] inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
                        >
                            <FaUpload className="mr-2" />
                            {t("upload")}
                        </button>
                    </div>
                </div>

                <fieldset disabled={loading || classInfo.gradeStatus === "LOCKED"}>
                    <div className="overflow-x-auto rounded-lg border border-gray-300 p-1">
                        <table className="min-w-full bg-gray-50 border rounded-lg overflow-hidden">
                            <thead className="bg-gray-50 ">
                                <tr>
                                    <th rowSpan={2} className="border border-gray-300 px-4 py-2 font-medium text-gray-700">
                                        {t('student-code')}
                                    </th>
                                    <th rowSpan={2} className="border border-gray-300 px-4 py-2 font-medium text-gray-700">
                                        {t('full-name')}
                                    </th>
                                    <th colSpan={displayCount} className="border border-gray-300 px-4 py-2 text-center font-medium text-gray-700">
                                        {t('extra-grades')}{" "}
                                        <button
                                            type="button"
                                            onClick={addExtra}
                                            disabled={extraCount >= MAX_EXTRA_GRADES}
                                            className="ml-2 inline-flex items-center px-2 py-1 text-xs border border-blue-500 text-blue-500 rounded hover:bg-blue-50 disabled:opacity-50"
                                        >
                                            <FaPlus />
                                        </button>
                                    </th>
                                    <th rowSpan={2} className="border border-gray-300 px-4 py-2 font-medium text-gray-700">
                                        {t('midterm-grade')}
                                    </th>
                                    <th rowSpan={2} className="border border-gray-300 px-4 py-2 font-medium text-gray-700">
                                        {t('final-grade')}
                                    </th>
                                </tr>
                                <tr>
                                    {Array.from({ length: displayCount }).map((_, idx) => (
                                        <th key={idx} className="border border-gray-300 px-4 py-2 text-center">
                                            {idx < extraCount && (
                                                <button
                                                    type="button"
                                                    onClick={() => removeExtraAt(idx)}
                                                    className="inline-flex items-center px-2 py-1 text-xs border border-red-500 text-red-500 rounded hover:bg-red-50"
                                                >
                                                    <FaTimes />
                                                </button>
                                            )}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody className="bg-gray-50">
                                {students.map((s) => (
                                    <tr key={s.studentId} className="hover:bg-gray-100">
                                        <td className="border border-gray-300 px-4 py-2 text-gray-700">{s.studentCode}</td>
                                        <td className="border border-gray-300 px-4 py-2 text-gray-700">{s.fullName}</td>

                                        {Array.from({ length: displayCount }).map((_, idx) => (
                                            <td key={idx} className="border border-gray-300 px-4 py-2">
                                                <input
                                                    type="number"
                                                    value={idx < extraCount ? s.extraGrades[idx] ?? "" : ""}
                                                    onChange={(e) =>
                                                        idx < extraCount &&
                                                        handleChange(s.studentId, "extra", e.target.value, idx)
                                                    }
                                                    min={0}
                                                    max={10}
                                                    step={0.1}
                                                    disabled={idx >= extraCount}
                                                    className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500
                                                        ${gradeErrors[s.studentId]?.extra?.[idx] ? 'border-red-500' : 'border-gray-300'}
                                                        ${idx >= extraCount ? 'bg-gray-100' : ''}`}
                                                />
                                                {gradeErrors[s.studentId]?.extra?.[idx] && (
                                                    <div className="text-red-500 text-xs mt-1">
                                                        {gradeErrors[s.studentId].extra![idx]}
                                                    </div>
                                                )}
                                            </td>
                                        ))}

                                        <td className="border border-gray-300 px-4 py-2">
                                            <input
                                                type="number"
                                                value={s.midtermGrade ?? ""}
                                                onChange={(e) =>
                                                    handleChange(s.studentId, "mid", e.target.value)
                                                }
                                                min={0}
                                                max={10}
                                                step={0.1}
                                                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500
                                                    ${gradeErrors[s.studentId]?.mid ? 'border-red-500' : 'border-gray-300'}`}
                                            />
                                            {gradeErrors[s.studentId]?.mid && (
                                                <div className="text-red-500 text-xs mt-1">
                                                    {gradeErrors[s.studentId].mid}
                                                </div>
                                            )}
                                        </td>
                                        <td className="border border-gray-300 px-4 py-2">
                                            <input
                                                type="number"
                                                value={s.finalGrade ?? ""}
                                                onChange={(e) =>
                                                    handleChange(s.studentId, "final", e.target.value)
                                                }
                                                min={0}
                                                max={10}
                                                step={0.1}
                                                className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500
                                                    ${gradeErrors[s.studentId]?.final ? 'border-red-500' : 'border-gray-300'}`}
                                            />
                                            {gradeErrors[s.studentId]?.final && (
                                                <div className="text-red-500 text-xs mt-1">
                                                    {gradeErrors[s.studentId].final}
                                                </div>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </fieldset>

                <div className="flex justify-between mt-6">
                    <button
                        onClick={saveGrades}
                        disabled={
                            loading || classInfo.gradeStatus === "LOCKED" || hasAnyGradeError()
                        }
                        className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
                    >
                        <FaSave className="mr-2" />
                        {capitalizeFirstWord(`${t("save")} ${t("grades-table")}`)}
                    </button>

                    <button
                        onClick={lockGrades}
                        disabled={loading || classInfo.gradeStatus === "LOCKED"}
                        className="inline-flex items-center px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
                    >
                        <FaLock className="mr-2" />
                        {capitalizeFirstWord(`${t("lock")} ${t("grades-table")}`)}
                    </button>
                </div>
            </div>
        </div>
    );
}