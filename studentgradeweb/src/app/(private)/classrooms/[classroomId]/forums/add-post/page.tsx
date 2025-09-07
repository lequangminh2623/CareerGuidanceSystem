import CreatePostClient from '@/components/forums/CreatePostClient';

export default async function CreatePostPage({ params }: { params: Promise<{ classroomId: string }> }) {
    const resolvedParams = await params;
    return <CreatePostClient classroomId={resolvedParams.classroomId} />;
}
