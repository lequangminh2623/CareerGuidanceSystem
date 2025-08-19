import ClassroomDetailClient from "@/components/classrooms/ClassroomDetailClient";

export default function ClassroomDetailPage({ params }: { params: { classroomId: string } }) {
    return <ClassroomDetailClient classroomId={params.classroomId} />;
}
