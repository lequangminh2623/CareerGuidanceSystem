import ForumPostDetailClient from "@/components/forums/ForumPostDetailClient";

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default async function ForumPostDetailPage({ params }: { params: Promise<Props['params']> }) {
    const resolvedParams = await params;
    return <ForumPostDetailClient classroomId={resolvedParams.classroomId} postId={resolvedParams.postId} />;
}
