import CreateReplyClient from '@/components/forums/CreateReplyClient';

interface Props {
    params: {
        classroomId: string;
        postId: string;
    };
}

export default function CreateReplyPage({ params }: Props) {
    return <CreateReplyClient classroomId={params.classroomId} postId={params.postId} />;
}
