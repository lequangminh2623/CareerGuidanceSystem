import ForumPostDetailClient from "@/components/forums/ForumPostDetailClient";

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default function ForumPostDetailPage({ params }: Props) {
    return <ForumPostDetailClient classroomId={params.classroomId} postId={params.postId} />;
}
