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
    lecturerName: string;
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
    lecturerName: string;
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
        lecturerName: "",
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
                lecturerName: data.lecturerName,
                courseName: data.courseName,
                academicTerm: data.academicTerm,
                gradeStatus: data.gradeStatus,
            });

            const studentsCopy: Student[] = data.students.map((s) => ({
                ...s,
                extraGrades: [...s.extraGrades],
            }));
            setStudents(studentsCopy);

            const maxEx = Math.min(
                MAX_EXTRA_GRADES,
                Math.max(0, ...studentsCopy.map((s) => s.extraGrades.length))
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
            "Bạn có chắc chắn muốn xóa cột điểm bổ sung này?"
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
                    error = "Điểm phải từ 0 đến 10";
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
            alert("Vui lòng chọn một file CSV!");
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
            alert(err.response?.data || "Lỗi khi upload file CSV");
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
            alert(err.response?.data || "Lỗi khi lưu điểm");
        } finally {
            setLoading(false);
        }
    };

    const lockGrades = async () => {
        const confirmed = window.confirm(
            "Bạn có chắc chắn muốn khóa bảng điểm? Sau khi khóa sẽ không thể chỉnh sửa!"
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
            alert(err.response?.data || "Lỗi khi khóa bảng điểm");
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
                throw new Error("Failed to download file");
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
            alert("Không thể tải file. Vui lòng thử lại.");
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
        <div className="container mx-auto px-4 py-6">
            {/* Header with Export Buttons */}
            <div className="flex justify-between items-center flex-wrap mb-6">
                <h3 className="text-2xl font-bold">
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

            {/* Class Info */}
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
                            {t("lecturer")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.lecturerName}
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
                            {t("academic-term")}:
                        </label>
                        <input
                            type="text"
                            value={classInfo.academicTerm}
                            readOnly
                            className="w-full px-3 py-2 bg-gray-100 border rounded-lg"
                        />
                    </div>
                </div>

                {/* CSV Upload */}
                <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                        Tải lên bảng điểm từ CSV:
                    </label>
                    <div className="flex gap-2">
                        <input
                            type="file"
                            accept=".csv"
                            onChange={handleFileChange}
                            disabled={classInfo.gradeStatus === "LOCKED"}
                            className="flex-1 px-3 py-2 border rounded-lg"
                        />
                        <button
                            onClick={uploadCsv}
                            disabled={loading || classInfo.gradeStatus === "LOCKED"}
                            className="inline-flex items-center px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
                        >
                            <FaUpload className="mr-2" />
                            Upload CSV
                        </button>
                    </div>
                </div>

                {/* TODO: Add full table rendering here (students, grades, errors) */}

                {/* Action Buttons */}
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
