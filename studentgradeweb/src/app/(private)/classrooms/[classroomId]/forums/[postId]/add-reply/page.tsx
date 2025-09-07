import CreateReplyClient from '@/components/forums/CreateReplyClient';

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default async function CreateReplyPage({ params }: { params: Promise<Props['params']> }) {
    const resolvedParams = await params;
    return <CreateReplyClient classroomId={resolvedParams.classroomId} postId={resolvedParams.postId} />;
}
