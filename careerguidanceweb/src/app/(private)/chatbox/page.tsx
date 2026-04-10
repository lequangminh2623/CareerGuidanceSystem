import ChatboxClient from "@/components/chat";

export default function ChatboxPage() {
    return (
        <div className="overflow-hidden">
            <div className="bg-linear-to-br from-blue-50 via-white to-indigo-50 p-4">
                <div className="max-w-7xl mx-auto space-y-4">
                    <div className="bg-white/40 backdrop-blur-md rounded-2xl shadow-2xl border border-white/50 overflow-hidden">
                        <ChatboxClient />
                    </div>
                </div>
            </div>
        </div>
    );
}
