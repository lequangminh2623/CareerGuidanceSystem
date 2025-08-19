import ForumClient from "@/components/forums/ForumClient";

export default async function ForumPage({ params }: { params: Promise<{ classroomId: string }> }) {
    const resolvedParams = await params;
    return <ForumClient classroomId={resolvedParams.classroomId} />;
}
