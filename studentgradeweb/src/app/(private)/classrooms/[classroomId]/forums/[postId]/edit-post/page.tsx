import EditPostClient from "@/components/forums/EditPostClient";

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default function EditPostPage({ params }: Props) {
    return <EditPostClient classroomId={params.classroomId} postId={params.postId} />;
}