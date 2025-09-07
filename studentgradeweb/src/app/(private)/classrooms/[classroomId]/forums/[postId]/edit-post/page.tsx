import EditPostClient from "@/components/forums/EditPostClient";

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default async function EditPostPage({ params }: { params: Promise<Props['params']> }) {
    const resolvedParams = await params;
    return <EditPostClient classroomId={resolvedParams.classroomId} postId={resolvedParams.postId} />;
}