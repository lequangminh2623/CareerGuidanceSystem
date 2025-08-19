import EditReplyClient from '@/components/forums/EditReplyClient';

interface Props {
    params: {
        classroomId: string;
        postId: string;
        replyId: string;
    };
}

export default function EditReplyPage({ params }: Props) {
    return <EditReplyClient {...params} />;
}
