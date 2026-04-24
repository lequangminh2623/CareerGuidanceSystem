"""
Holland RIASEC career guidance router.
Endpoint for Flow 1: Career recommendation based on Holland codes.
"""

import logging
from fastapi import APIRouter, HTTPException

from app.models.schemas import (
    HollandGuidanceRequest,
    HollandGuidanceResponse,
    ErrorResponse,
)
from app.services import gemini_service, chat_manager
from app.services.redis_cache import get_cached, set_cached

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/holland", tags=["Holland Guidance"])


@router.post(
    "/guidance",
    response_model=HollandGuidanceResponse,
    responses={
        500: {"model": ErrorResponse, "description": "Gemini API error"},
    },
    summary="Tư vấn nghề nghiệp dựa trên mã Holland",
    description=(
        "Nhận 3 mã Holland (RIASEC) cao nhất từ bài trắc nghiệm và trả về "
        "gợi ý nghề nghiệp phù hợp từ mô hình AI đã fine-tune."
    ),
)
async def holland_guidance(request: HollandGuidanceRequest):
    """
    Flow 1: Holland-based career guidance.

    Sends the exact prompt format that the fine-tuned model was trained on:
    "Tôi có kết quả trắc nghiệm Holland với 3 mã cao nhất là: X, Y, Z.
     Nghề nghiệp nào phù hợp với tôi?"
    """
    try:
        # Check cache first
        cache_input = {"holland_codes": request.holland_codes}
        cached_result = get_cached("holland", cache_input)
        if cached_result:
            result = cached_result
        else:
            result = gemini_service.get_holland_guidance(request.holland_codes)
            # Store in cache
            set_cached("holland", cache_input, result)

        # Create a chat session with the Holland result as context
        session_id = chat_manager.create_session(
            initial_context={"holland_result": result}
        )

        return HollandGuidanceResponse(
            holland_result=result,
            session_id=session_id,
        )

    except gemini_service.GeminiApiError as e:
        logger.error("Holland guidance error: %s", e.message)
        raise HTTPException(status_code=500, detail=e.message)

    except Exception as e:
        logger.error("Unexpected error in Holland guidance: %s", str(e))
        raise HTTPException(
            status_code=500,
            detail="Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
        )
