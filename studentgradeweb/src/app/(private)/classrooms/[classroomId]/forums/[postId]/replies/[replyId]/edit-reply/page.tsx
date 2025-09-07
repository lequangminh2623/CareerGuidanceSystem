import EditReplyClient from '@/components/forums/EditReplyClient';

interface Props {
    params: {
        classroomId: string;
        postId: string;
        replyId: string;
    };
}

export default async function EditReplyPage({ params }: { params: Promise<Props["params"]> }) {
    const resolvedParams = await params;
    return (
        <EditReplyClient
            classroomId={resolvedParams.classroomId}
            postId={resolvedParams.postId}
            replyId={resolvedParams.replyId}
        />
    );
}
