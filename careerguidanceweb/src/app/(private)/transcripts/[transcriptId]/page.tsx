import TranscriptDetailClient from "@/components/transcripts/TranscriptDetailClient";

export default async function ClassroomDetailPage({ params }: { params: Promise<{ transcriptId: string }> }) {
    const resolvedParams = await params;
    return <TranscriptDetailClient sectionId={resolvedParams.transcriptId} />;
}
