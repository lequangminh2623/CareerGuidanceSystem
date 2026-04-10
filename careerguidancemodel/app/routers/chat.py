"""
Follow-up chat router.
Enables continued conversation after initial career guidance.
"""

import logging
from fastapi import APIRouter, HTTPException

from app.models.schemas import (
    ChatMessageRequest,
    ChatMessageResponse,
    ChatHistoryResponse,
    ChatHistoryMessage,
    ErrorResponse,
)
from app.services import gemini_service, chat_manager

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/chat", tags=["Follow-up Chat"])


@router.post(
    "",
    response_model=ChatMessageResponse,
    responses={
        404: {"model": ErrorResponse, "description": "Session not found"},
        500: {"model": ErrorResponse, "description": "Gemini API error"},
    },
    summary="Gửi tin nhắn tiếp theo trong phiên tư vấn",
    description=(
        "Gửi câu hỏi hoặc tin nhắn tiếp theo dựa trên kết quả tư vấn ban đầu. "
        "Hệ thống sẽ sử dụng bối cảnh của phiên trước đó để trả lời phù hợp."
    ),
)
async def send_chat_message(request: ChatMessageRequest):
    """
    Send a follow-up message in an existing chat session.
    The Gemini model will respond with career counselor context.
    """
    session = chat_manager.get_session(request.session_id)
    if session is None:
        raise HTTPException(
            status_code=404,
            detail="Phiên tư vấn không tồn tại hoặc đã hết hạn. Vui lòng bắt đầu lại.",
        )

    try:
        # Get existing history and initial context
        history = chat_manager.get_history(request.session_id) or []
        initial_context = chat_manager.get_initial_context(request.session_id)

        # Call Gemini with full context
        reply = gemini_service.chat_followup(
            message=request.message,
            history=history,
            initial_context=initial_context if len(history) == 0 else None,
        )

        # Store both user message and model reply in history
        chat_manager.add_message(request.session_id, "user", request.message)
        chat_manager.add_message(request.session_id, "model", reply)

        return ChatMessageResponse(
            reply=reply,
            session_id=request.session_id,
        )

    except gemini_service.GeminiApiError as e:
        logger.error("Chat error: %s", e.message)
        raise HTTPException(status_code=500, detail=e.message)

    except Exception as e:
        logger.error("Unexpected chat error: %s", str(e))
        raise HTTPException(
            status_code=500,
            detail="Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
        )


@router.get(
    "/{session_id}/history",
    response_model=ChatHistoryResponse,
    responses={
        404: {"model": ErrorResponse, "description": "Session not found"},
    },
    summary="Lấy lịch sử hội thoại",
    description="Trả về toàn bộ lịch sử hội thoại của một phiên tư vấn.",
)
async def get_chat_history(session_id: str):
    """
    Retrieve the full conversation history for a chat session.
    """
    history = chat_manager.get_history(session_id)
    if history is None:
        raise HTTPException(
            status_code=404,
            detail="Phiên tư vấn không tồn tại hoặc đã hết hạn.",
        )

    return ChatHistoryResponse(
        session_id=session_id,
        messages=[
            ChatHistoryMessage(role=msg["role"], content=msg["content"])
            for msg in history
        ],
    )
