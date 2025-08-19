import CreatePostClient from '@/components/forums/CreatePostClient';

export default function CreatePostPage({ params }: { params: { classroomId: string } }) {
    return <CreatePostClient classroomId={params.classroomId} />;
}
