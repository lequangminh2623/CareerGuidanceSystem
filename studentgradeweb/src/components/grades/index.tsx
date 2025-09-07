'use client';

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { useTranslation } from "react-i18next";
import { authApis, endpoints } from "@/lib/utils/api";
import { capitalizeFirstWord } from "@/lib/utils";
import MySpinner from "@/components/layout/MySpinner";
import SemesterTable from "./SemesterTable";

interface Grade {
    gradeDetail: {
        semester: {
            academicYear: {
                year: string;
            };
            semesterType: string;
        };
        course: {
            id: string;
            name: string;
            credit: number;
        };
        extraGradeSet: Array<{ grade: number }>;
        midtermGrade: number | null;
        finalGrade: number | null;
    };
    classroomName: string;
}

interface SemesterGroup {
    [key: string]: {
        semesterTitle: string;
        subjects: Array<{
            code: string;
            classCode: string;
            name: string;
            credit: number;
            extraGrade: number[];
            midTermGrade: number | null;
            finalGrade: number | null;
        }>;
    };
}

const GradesClient = () => {
    const [gradesBySemester, setGradesBySemester] = useState<Array<{
        semesterTitle: string;
        subjects: Array<{
            code: string;
            classCode: string;
            name: string;
            credit: number;
            extraGrade: number[];
            midTermGrade: number | null;
            finalGrade: number | null;
        }>;
    }>>([]);
    const [loading, setLoading] = useState(false);
    const searchParams = useSearchParams();
    const { i18n, t } = useTranslation();

    const loadGrades = async () => {
        try {
            setLoading(true);
            let url = endpoints['student-grades'];
            const kw = searchParams.get('kw');

            if (kw) {
                url = `${url}?kw=${kw}`;
            }

            const res = await authApis().get(url);
            const data: Grade[] = res.data;

            const grouped = data.reduce<SemesterGroup>((idx, grade) => {
                const semesterKey = `${grade.gradeDetail.semester.academicYear.year} - ${grade.gradeDetail.semester.semesterType}`;
                if (!idx[semesterKey]) {
                    idx[semesterKey] = {
                        semesterTitle: i18n.language === "vi" ?
                            `${t('semester')} ${t(`semesterTypes.${grade.gradeDetail.semester.semesterType}`)}
                        - ${t('year')} ${grade.gradeDetail.semester.academicYear.year}` :
                            `${t(`semesterTypes.${grade.gradeDetail.semester.semesterType}`)}
                        - ${grade.gradeDetail.semester.academicYear.year}`,
                        subjects: [],
                    };
                }
                idx[semesterKey].subjects.push({
                    code: grade.gradeDetail.course.id,
                    classCode: grade.classroomName,
                    name: grade.gradeDetail.course.name,
                    credit: grade.gradeDetail.course.credit,
                    extraGrade: grade.gradeDetail.extraGradeSet.map(i => i.grade),
                    midTermGrade: grade.gradeDetail.midtermGrade,
                    finalGrade: grade.gradeDetail.finalGrade
                });
                return idx;
            }, {});

            const groupedArray = Object.values(grouped);
            setGradesBySemester(groupedArray);

        } catch (ex) {
            console.error(ex);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadGrades();
    }, [searchParams, i18n.language]);

    return (
        <div className="container mx-auto px-4 py-6 min-h-screen">
            <h3 className="text-2xl font-bold mb-6">{t('grades-table')}</h3>

            {gradesBySemester.length > 0 ? (
                gradesBySemester.map((semester, idx) => (
                    <SemesterTable
                        key={idx}
                        semesterTitle={semester.semesterTitle}
                        subjects={semester.subjects}
                    />
                ))
            ) : (
                <div className="bg-blue-50 border-l-4 border-blue-400 p-4 text-blue-700">
                    {capitalizeFirstWord(`${t('none')} ${t('grades')}`)}
                </div>
            )}

            {loading && <MySpinner />}
        </div>
    );
};

export default GradesClient;
