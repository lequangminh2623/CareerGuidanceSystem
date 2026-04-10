'use client';

import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import { guidanceApi, guidanceEndpoints } from '@/lib/utils/api';
import { FiMessageCircle, FiX, FiSend, FiMinimize2 } from 'react-icons/fi';

interface ChatMsg {
    role: 'user' | 'model';
    content: string;
}

interface Props {
    sessionId: string | null;
}

export default function ChatWidget({ sessionId }: Props) {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState<ChatMsg[]>([]);
    const [input, setInput] = useState('');
    const [isSending, setIsSending] = useState(false);
    const scrollRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    // Auto-scroll on new messages
    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    // Focus input when opened
    useEffect(() => {
        if (isOpen && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isOpen]);

    const sendMessage = async () => {
        if (!input.trim() || !sessionId || isSending) return;

        const userMsg = input.trim();
        setInput('');
        setMessages(prev => [...prev, { role: 'user', content: userMsg }]);
        setIsSending(true);

        try {
            const res = await guidanceApi.post(guidanceEndpoints.chat, {
                message: userMsg,
                session_id: sessionId,
            });
            setMessages(prev => [...prev, { role: 'model', content: res.data.reply }]);
        } catch {
            setMessages(prev => [...prev, {
                role: 'model',
                content: 'Xin lỗi, đã xảy ra lỗi. Vui lòng thử lại.',
            }]);
        } finally {
            setIsSending(false);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    // Don't render if no session
    if (!sessionId) return null;

    return (
        <>
            {/* ══════ Floating Button ══════ */}
            {!isOpen && (
                <button
                    onClick={() => setIsOpen(true)}
                    className="fixed bottom-6 right-6 z-50 w-14 h-14 bg-gray-900 text-white rounded-full shadow-xl flex items-center justify-center hover:scale-105 transition-all duration-300 active:scale-95"
                    title="Hỏi thêm chuyên gia tư vấn"
                >
                    <FiMessageCircle className="w-6 h-6" />
                    {/* Indicator dot */}
                    <span className="absolute -top-1 -right-1 w-3.5 h-3.5 bg-emerald-500 rounded-full border-2 border-white" />
                </button>
            )}

            {/* ══════ Chat Panel ══════ */}
            {isOpen && (
                <div className="fixed bottom-6 right-6 z-50 w-[380px] max-w-[calc(100vw-2rem)] h-[520px] max-h-[calc(100vh-6rem)] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden animate-in slide-in-from-bottom-4 fade-in duration-300">
                    {/* Header */}
                    <div className="bg-gray-900 px-5 py-4 flex items-center justify-between shrink-0">
                        <div className="flex items-center gap-3">
                            <div>
                                <h4 className="text-white font-bold text-sm tracking-tight">Tư vấn hướng nghiệp</h4>
                                <div className="flex items-center gap-1.5">
                                    <div className="w-1.5 h-1.5 bg-emerald-400 rounded-full" />
                                    <span className="text-white/60 text-[10px] font-bold uppercase tracking-wider">Trực tuyến</span>
                                </div>
                            </div>
                        </div>
                        <div className="flex items-center gap-1">
                            <button
                                onClick={() => setIsOpen(false)}
                                className="w-8 h-8 rounded-lg hover:bg-white/10 flex items-center justify-center text-white/80 transition-colors"
                            >
                                <FiMinimize2 className="w-4 h-4" />
                            </button>
                            <button
                                onClick={() => setIsOpen(false)}
                                className="w-8 h-8 rounded-lg hover:bg-white/10 flex items-center justify-center text-white/80 transition-colors"
                            >
                                <FiX className="w-4 h-4" />
                            </button>
                        </div>
                    </div>

                    {/* Messages */}
                    <div ref={scrollRef} className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50/50">
                        {/* Welcome message */}
                        {messages.length === 0 && (
                            <div className="text-center py-8 space-y-3">
                                <p className="text-xs font-bold text-gray-400 uppercase tracking-widest">
                                    Bắt đầu cuộc trò chuyện
                                </p>
                                <p className="text-sm text-gray-500 max-w-[250px] mx-auto">
                                    Hãy đặt câu hỏi để được tư vấn chuyên sâu hơn dựa trên kết quả phân tích của bạn.
                                </p>
                                <div className="flex flex-wrap gap-2 justify-center">
                                    {['Tôi nên chọn trường nào?', 'Ngành nào lương cao?', 'Cần chuẩn bị gì?'].map(hint => (
                                        <button
                                            key={hint}
                                            onClick={() => { setInput(hint); inputRef.current?.focus(); }}
                                            className="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs font-medium text-gray-600 hover:border-indigo-300 hover:text-indigo-600 transition-colors"
                                        >
                                            {hint}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        {messages.map((msg, i) => (
                            <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                <div className={`max-w-[85%] rounded-2xl px-4 py-3 ${msg.role === 'user'
                                    ? 'bg-gray-900 text-white rounded-br-md'
                                    : 'bg-white border border-gray-100 shadow-sm text-gray-700 rounded-bl-md'
                                    }`}>
                                    {msg.role === 'model' ? (
                                        <div className="prose prose-sm max-w-none text-gray-700 prose-p:my-1 prose-li:my-0.5 prose-headings:text-gray-900 prose-headings:text-sm">
                                            <ReactMarkdown>{msg.content}</ReactMarkdown>
                                        </div>
                                    ) : (
                                        <p className="text-sm">{msg.content}</p>
                                    )}
                                </div>
                            </div>
                        ))}

                        {/* Typing indicator */}
                        {isSending && (
                            <div className="flex justify-start">
                                <div className="bg-white border border-gray-100 shadow-sm rounded-2xl rounded-bl-md px-4 py-3 flex items-center gap-1.5">
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Input */}
                    <div className="p-3 border-t border-gray-100 bg-white shrink-0">
                        <div className="flex items-center gap-2">
                            <input
                                ref={inputRef}
                                type="text"
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                onKeyDown={handleKeyDown}
                                placeholder="Nhập câu hỏi..."
                                disabled={isSending}
                                className="flex-1 px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all disabled:opacity-50 text-gray-900 placeholder:text-gray-400"
                            />
                            <button
                                onClick={sendMessage}
                                disabled={!input.trim() || isSending}
                                className="w-10 h-10 bg-gray-900 text-white rounded-xl flex items-center justify-center hover:shadow-md transition-all disabled:opacity-40 disabled:cursor-not-allowed shrink-0"
                            >
                                <FiSend className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
