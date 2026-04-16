'use client';

import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { authApis, endpoints } from "@/lib/utils/api";
import {
    useGetTranscriptDetailQuery,
    useSaveScoresMutation,
    useImportScoresCsvMutation,
    useLockScoresMutation,
    type SectionResponseDTO,
    type ExceptionResponseDTO,
} from "@/store/features/api/apiSlice";
import { useAppDispatch } from "@/store/hooks";
import { showNotification } from "@/store/features/ui/uiSlice";
import MySpinner from "@/components/layout/MySpinner";
import { FaSave, FaUpload, FaLock, FaFileCsv, FaFilePdf } from "react-icons/fa";
import { useTranslation } from "react-i18next";
import { capitalizeFirstWord } from "@/lib/utils";
import ScoreTable from "./ScoreTable";
import Button from "../ui/Button";
import Modal from "../ui/Modal";

// ===== Types =====
export interface ExtraScore {
    id?: number;
    score: number | null;
    scoreIndex: number;
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

const MAX_EXTRA_SCORES = 5;

// Helper: merge API data into StudentData[]
function buildStudents(data: { section: SectionResponseDTO; scores: { studentId: string; midtermScore: number | null; finalScore: number | null; extraScores: (number | null)[] }[]; students: Record<string, { id: string; code: string; firstName: string; lastName: string }> }): StudentData[] {
    const merged: StudentData[] = Object.values(data.students).map((user) => {
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
    merged.sort((a, b) => a.fullName.toLowerCase().localeCompare(b.fullName.toLowerCase()));
    return merged;
}

export default function TranscriptDetailClient({ sectionId }: Props) {
    const q = useSearchParams();
    const kw = q.get("kw") ?? undefined;
    const { t } = useTranslation();
    const dispatch = useAppDispatch();

    // ── RTK Query: GET transcript detail ──
    const { data: transcriptData, isLoading: fetching, refetch } = useGetTranscriptDetailQuery({ sectionId, kw });

    // ── RTK Mutations ──
    const [saveScoresMutation, { isLoading: saving }] = useSaveScoresMutation();
    const [importScoresCsvMutation, { isLoading: importing }] = useImportScoresCsvMutation();
    const [lockScoresMutation, { isLoading: locking }] = useLockScoresMutation();

    const loading = fetching || saving || importing || locking;

    // ── Local state: editable grid data ──
    const [students, setStudents] = useState<StudentData[]>([]);
    const [sectionInfo, setSectionInfo] = useState<SectionResponseDTO>({
        id: "", classroomId: "", teacherName: "", classroomName: "",
        gradeName: "", yearName: "", semesterName: "", subjectName: "", scoreStatus: "",
    });
    const [scoreErrors, setScoreErrors] = useState<ScoreErrors>({});
    const [extraCount, setExtraCount] = useState<number>(0);
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [confirmDeleteIdx, setConfirmDeleteIdx] = useState<number | null>(null);
    const [confirmLockModal, setConfirmLockModal] = useState<boolean>(false);

    // Sync local editable state when RTK Query data arrives
    useEffect(() => {
        if (!transcriptData) return;
        setSectionInfo(transcriptData.section);
        const merged = buildStudents(transcriptData);
        setStudents(merged);
        const maxEx = Math.min(MAX_EXTRA_SCORES, Math.max(0, ...merged.map(s => s.extraScores?.length || 0)));
        setExtraCount(maxEx);
    }, [transcriptData]);

    // ===== Extra columns =====
    const addExtra = () => {
        if (extraCount < MAX_EXTRA_SCORES) {
            setExtraCount(extraCount + 1);
            setStudents(prev => prev.map(s => ({ ...s, extraScores: [...s.extraScores, null] })));
        }
    };

    const removeExtraAt = (idx: number) => setConfirmDeleteIdx(idx);

    const executeRemoveExtra = () => {
        if (confirmDeleteIdx === null) return;
        const idx = confirmDeleteIdx;
        setExtraCount(prev => prev - 1);
        setStudents(prev => prev.map(s => {
            const arr = [...s.extraScores];
            arr.splice(idx, 1);
            return { ...s, extraScores: arr };
        }));
        setConfirmDeleteIdx(null);
    };

    // ===== Cell change handler =====
    const handleChange = (studentId: string, field: "mid" | "final" | "extra", value: string, idx?: number) => {
        setStudents(prev => prev.map(s => {
            if (s.studentId !== studentId) return s;
            const val = value === "" ? null : parseFloat(value);
            const error = val !== null && (val < 0 || val > 10) ? t("score-range-error") : "";

            setScoreErrors(prevErrs => {
                const errs = { ...prevErrs };
                if (!errs[studentId]) errs[studentId] = {};
                if (field === "extra" && idx !== undefined) {
                    if (!errs[studentId].extra) errs[studentId].extra = {};
                    errs[studentId].extra![idx] = error;
                } else {
                    errs[studentId][field as "mid" | "final"] = error;
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
        }));
    };

    // ===== File upload =====
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const uploadCsv = async () => {
        if (!selectedFile) {
            dispatch(showNotification({
                type: 'warning',
                title: t('warning'),
                message: t('empty-error'),
            }));
            return;
        }
        const formData = new FormData();
        formData.append("file", selectedFile);
        try {
            const result = await importScoresCsvMutation({ sectionId, formData }).unwrap();
            dispatch(showNotification({
                type: 'success',
                title: t('success'),
                message: result,
            }));
        } catch (err) {
            const data = (err as { data?: ExceptionResponseDTO | string })?.data;
            const msg = typeof data === 'string' ? data : (data as ExceptionResponseDTO)?.message;
            const finalMsg = msg || t("error-message");
            const isActuallySuccess = finalMsg.toLowerCase().includes("success") || finalMsg.toLowerCase().includes("thành công");

            dispatch(showNotification({
                type: isActuallySuccess ? 'success' : 'error',
                title: isActuallySuccess ? t('success') : t('error'),
                message: finalMsg,
            }));
        }
    };

    const saveScores = async () => {
        const scoreList = students.map(s => ({
            studentId: s.studentId,
            midtermScore: s.midtermScore,
            finalScore: s.finalScore,
            extraScores: s.extraScores.slice(0, extraCount) as (number | null)[],
        }));
        try {
            const result = await saveScoresMutation({ sectionId, scores: scoreList }).unwrap();
            dispatch(showNotification({
                type: 'success',
                title: t('success'),
                message: result,
            }));
        } catch (err) {
            const data = (err as { data?: ExceptionResponseDTO | string })?.data;
            const msg = typeof data === 'string' ? data : (data as ExceptionResponseDTO)?.message;
            const finalMsg = msg || t("error-message");
            const isActuallySuccess = finalMsg.toLowerCase().includes("success") || finalMsg.toLowerCase().includes("thành công");

            dispatch(showNotification({
                type: isActuallySuccess ? 'success' : 'error',
                title: isActuallySuccess ? t('success') : t('error'),
                message: finalMsg,
            }));
        }
    };

    const lockScores = async () => {
        setConfirmLockModal(false);
        try {
            const result = await lockScoresMutation(sectionId).unwrap();
            setSectionInfo(prev => ({ ...prev, scoreStatus: "Locked" }));
            dispatch(showNotification({
                type: 'success',
                title: t('success'),
                message: result,
            }));
        } catch (err) {
            const data = (err as { data?: ExceptionResponseDTO | string })?.data;
            const msg = typeof data === 'string' ? data : (data as ExceptionResponseDTO)?.message;
            const finalMsg = msg || t("error-message");
            const isActuallySuccess = finalMsg.toLowerCase().includes("success") || finalMsg.toLowerCase().includes("thành công");

            dispatch(showNotification({
                type: isActuallySuccess ? 'success' : 'error',
                title: isActuallySuccess ? t('success') : t('error'),
                message: finalMsg,
            }));
        }
    };

    const downloadFile = async (format: "csv" | "pdf") => {
        try {
            const response = await authApis().get(
                endpoints[`export-${format}`](sectionId),
                { responseType: "blob", withCredentials: true }
            );
            const blob = new Blob([response.data], { type: response.data.type });
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = downloadUrl;
            link.download = `scores_section_${sectionId}.${format}`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(downloadUrl);
        } catch {
            dispatch(showNotification({
                type: 'error',
                title: t('error'),
                message: t("error-message") || "Lỗi tải file",
            }));
        }
    };

    const hasAnyScoreError = () => {
        for (const sid in scoreErrors) {
            if (!scoreErrors[sid]) continue;
            if (scoreErrors[sid].mid || scoreErrors[sid].final) return true;
            if (scoreErrors[sid].extra) {
                for (const idx in scoreErrors[sid].extra) {
                    if (scoreErrors[sid].extra![idx]) return true;
                }
            }
        }
        return false;
    };

    if (loading && !students.length) return <div className="min-h-screen flex items-center justify-center"><MySpinner /></div>;

    return (
        <>
            <div className="container mx-auto px-4 py-8">
                {/* Header Card */}
                <div className="bg-white p-6 md:p-8 rounded-2xl shadow-sm border border-gray-100 mb-8 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-64 h-64 bg-linear-to-br from-blue-50 to-indigo-50 rounded-full opacity-50 transform translate-x-1/3 -translate-y-1/3 pointer-events-none" />
                    
                    <div className="relative z-10 flex flex-col md:flex-row md:items-start justify-between gap-6 mb-8">
                        <div>
                            <div className="flex items-center gap-3 mb-2">
                                <h2 className="text-3xl font-bold text-gray-900 tracking-tight">
                                    {t('transcript-of-class')} {sectionInfo.classroomName || "..."}
                                </h2>
                                <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shadow-sm border
                                    ${sectionInfo.scoreStatus === 'Locked' ? 'bg-red-50 text-red-600 border-red-200' : 'bg-green-50 text-green-600 border-green-200'}`}>
                                    {sectionInfo.scoreStatus === 'Locked' ? t('locked') : t('unlocked')}
                                </span>
                            </div>
                        </div>
                        <div className="flex flex-wrap gap-2">
                            <Button variant="outline" onClick={() => downloadFile("csv")} disabled={sectionInfo.scoreStatus !== "Locked"} icon={<FaFileCsv className="text-green-600" />}>
                                {t('export-csv')}
                            </Button>
                            <Button variant="outline" onClick={() => downloadFile("pdf")} disabled={sectionInfo.scoreStatus !== "Locked"} icon={<FaFilePdf className="text-red-500" />}>
                                {t('export-pdf')}
                            </Button>
                        </div>
                    </div>

                    <div className="relative z-10 grid grid-cols-2 md:grid-cols-5 gap-4 p-5 bg-gray-50/80 rounded-2xl border border-gray-100">
                        {[
                            { label: t('subject'), value: sectionInfo.subjectName },
                            { label: t('grade'), value: sectionInfo.gradeName },
                            { label: t('semester'), value: sectionInfo.semesterName },
                            { label: t('year'), value: sectionInfo.yearName },
                            { label: t('teacher-in-charge'), value: sectionInfo.teacherName || t('not-assigned') },
                        ].map(({ label, value }) => (
                            <div key={label} className="flex flex-col">
                                <span className="text-[11px] text-gray-400 font-bold uppercase tracking-wider mb-1">{label}</span>
                                <span className="text-gray-800 font-semibold">{value || "—"}</span>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Score Management Card */}
                <div className="bg-white p-6 md:p-8 rounded-2xl shadow-sm border border-gray-100 mb-8">
                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-6">
                        <div className="flex-1">
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                <FaUpload className="inline mr-2 text-gray-400" />{t('upload-from-csv')}
                            </label>
                            <div className="flex flex-wrap md:flex-nowrap gap-3">
                                <input
                                    type="file"
                                    accept=".csv, .xlsx, .xls"
                                    onChange={handleFileChange}
                                    disabled={loading || sectionInfo.scoreStatus === "Locked"}
                                    className="w-full md:max-w-md bg-gray-50 px-4 text-gray-400 py-2 border border-gray-200 rounded-xl file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-medium disabled:opacity-50 file:bg-blue-600 file:text-white hover:file:bg-blue-700 disabled:bg-gray-100 transition focus:outline-none focus:border-blue-300"
                                />
                                <Button onClick={uploadCsv} isLoading={importing} disabled={sectionInfo.scoreStatus === "Locked"}>
                                    {t('upload-file')}
                                </Button>
                            </div>
                        </div>
                    </div>

                    <ScoreTable 
                        students={students}
                        extraCount={extraCount}
                        maxExtra={MAX_EXTRA_SCORES}
                        scoreErrors={scoreErrors}
                        isLocked={sectionInfo.scoreStatus === "Locked"}
                        onScoreChange={handleChange}
                        onAddExtra={addExtra}
                        onRemoveExtra={removeExtraAt}
                    />

                    <div className="flex flex-col sm:flex-row justify-between items-center gap-4 mt-6">
                        <p className="text-sm text-gray-500">{t('score-validation-hint')}</p>
                        <div className="flex gap-3 w-full sm:w-auto">
                            <Button onClick={saveScores} isLoading={saving} disabled={sectionInfo.scoreStatus === "Locked" || hasAnyScoreError()} icon={<FaSave />}>
                                {capitalizeFirstWord(`${t("save")} ${t("scores-table")}`)}
                            </Button>
                            <Button variant="danger" onClick={() => setConfirmLockModal(true)} isLoading={locking} disabled={sectionInfo.scoreStatus === "Locked"} icon={<FaLock />}>
                                {capitalizeFirstWord(`${t("lock")} ${t("scores-table")}`)}
                            </Button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Modals */}
            <Modal
                isOpen={confirmDeleteIdx !== null}
                onClose={() => setConfirmDeleteIdx(null)}
                title={t('delete-column')}
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setConfirmDeleteIdx(null)}>{t('cancel-action')}</Button>
                        <Button variant="danger" onClick={executeRemoveExtra}>{t('delete-confirm')}</Button>
                    </>
                }
            >
                <p className="text-sm text-gray-600 font-medium">{t('confirm-delete-column')}</p>
            </Modal>

            <Modal
                isOpen={confirmLockModal}
                onClose={() => setConfirmLockModal(false)}
                title={t('lock-transcript-title')}
                footer={
                    <>
                        <Button variant="ghost" onClick={() => setConfirmLockModal(false)}>{t('back')}</Button>
                        <Button variant="danger" onClick={lockScores}>{t('lock-confirm')}</Button>
                    </>
                }
            >
                <p className="text-sm text-gray-600 font-medium leading-relaxed">
                    {t('lock-transcript-confirm', { subject: sectionInfo.subjectName })}
                </p>
            </Modal>
        </>
    );
}
