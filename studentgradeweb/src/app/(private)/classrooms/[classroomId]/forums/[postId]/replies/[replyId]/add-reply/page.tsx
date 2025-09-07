import CreateReplyClient from '@/components/forums/CreateReplyClient';

interface Props {
    params: {
        classroomId: string;
        postId: string;
        replyId?: string;
    };
}

export default async function CreateSubReplyPage({ params }: { params: Promise<Props['params']> }) {
    const resolvedParams = await params;
    return <CreateReplyClient classroomId={resolvedParams.classroomId} postId={resolvedParams.postId} parentId={resolvedParams.replyId} />;
}
