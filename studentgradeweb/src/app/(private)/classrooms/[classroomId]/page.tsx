import ClassroomDetailClient from "@/components/classrooms/ClassroomDetailClient";

export default async function ClassroomDetailPage({ params }: { params: Promise<{ classroomId: string }> }) {
    const resolvedParams = await params;
    return <ClassroomDetailClient classroomId={resolvedParams.classroomId} />;
}
