"""
Career guidance router.
Endpoints for Flow 2 (academic analysis) and combined guidance (both flows).
Also supports auto-fetching data from Spring Boot.
"""

import logging
from fastapi import APIRouter, HTTPException, Header
from typing import Optional

from app.models.schemas import (
    AcademicGuidanceRequest,
    AcademicGuidanceResponse,
    FullGuidanceRequest,
    FullGuidanceResponse,
    SpringBootGuidanceRequest,
    StudentScores,
    StudentProfile,
    ErrorResponse,
)
from app.services import gemini_service, chat_manager
from app.services.spring_boot_client import (
    get_student_scores,
    get_current_user,
    aggregate_subject_scores,
    map_scores_to_schema,
    SpringBootApiError,
    SpringBootConnectionError,
)

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/guidance", tags=["Career Guidance"])


# ══════════════════════════════════════════════
#  Flow 2: Academic Analysis Guidance
# ══════════════════════════════════════════════


@router.post(
    "/academic",
    response_model=AcademicGuidanceResponse,
    responses={
        500: {"model": ErrorResponse, "description": "Gemini API error"},
    },
    summary="Phân tích học tập và tư vấn nghề nghiệp",
    description=(
        "Nhận điểm số 7 môn, thông tin cá nhân và thói quen học tập. "
        "Trả về phân tích và gợi ý nghề nghiệp từ mô hình AI đã fine-tune."
    ),
)
async def academic_guidance(request: AcademicGuidanceRequest):
    """
    Flow 2: Academic analysis career guidance.

    Sends the exact prompt format matching the fine-tuned model's training data.
    """
    try:
        result = gemini_service.get_academic_guidance(
            scores=request.scores,
            profile=request.profile,
            survey=request.survey,
        )

        session_id = chat_manager.create_session(
            initial_context={"academic_result": result}
        )

        return AcademicGuidanceResponse(
            academic_result=result,
            session_id=session_id,
        )

    except gemini_service.GeminiApiError as e:
        logger.error("Academic guidance error: %s", e.message)
        raise HTTPException(status_code=500, detail=e.message)

    except Exception as e:
        logger.error("Unexpected error in academic guidance: %s", str(e))
        raise HTTPException(
            status_code=500,
            detail="Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
        )


# ══════════════════════════════════════════════
#  Combined: Both Flows
# ══════════════════════════════════════════════


@router.post(
    "/full",
    response_model=FullGuidanceResponse,
    responses={
        500: {"model": ErrorResponse, "description": "Gemini API error"},
    },
    summary="Tư vấn toàn diện (Holland + Phân tích học tập)",
    description=(
        "Chạy cả 2 luồng tư vấn riêng biệt: "
        "(1) Tư vấn dựa trên mã Holland, "
        "(2) Phân tích dựa trên điểm số và thói quen học tập. "
        "Trả về kết quả từ cả 2 luồng và tạo phiên chat follow-up."
    ),
)
async def full_guidance(request: FullGuidanceRequest):
    """
    Combined guidance: runs both Flow 1 (Holland) and Flow 2 (Academic)
    SEPARATELY, then creates a chat session with both results as context.
    """
    try:
        # Flow 1: Holland guidance
        holland_result = gemini_service.get_holland_guidance(
            request.holland_codes
        )

        # Flow 2: Academic guidance
        academic_result = gemini_service.get_academic_guidance(
            scores=request.scores,
            profile=request.profile,
            survey=request.survey,
        )

        # Create chat session with both results as context
        session_id = chat_manager.create_session(
            initial_context={
                "holland_result": holland_result,
                "academic_result": academic_result,
            }
        )

        return FullGuidanceResponse(
            holland_result=holland_result,
            academic_result=academic_result,
            session_id=session_id,
        )

    except gemini_service.GeminiApiError as e:
        logger.error("Full guidance error: %s", e.message)
        raise HTTPException(status_code=500, detail=e.message)

    except Exception as e:
        logger.error("Unexpected error in full guidance: %s", str(e))
        raise HTTPException(
            status_code=500,
            detail="Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
        )


# ══════════════════════════════════════════════
#  Auto-Fetch from Spring Boot
# ══════════════════════════════════════════════


@router.post(
    "/from-springboot",
    response_model=FullGuidanceResponse,
    responses={
        401: {"model": ErrorResponse, "description": "Unauthorized"},
        500: {"model": ErrorResponse, "description": "API error"},
        503: {"model": ErrorResponse, "description": "Service unavailable"},
    },
    summary="Tư vấn tự động từ dữ liệu hệ thống",
    description=(
        "Tự động lấy điểm số và thông tin học sinh từ hệ thống Spring Boot "
        "(yêu cầu JWT token). Kết hợp với dữ liệu khảo sát và mã Holland "
        "từ frontend để chạy cả 2 luồng tư vấn."
    ),
)
async def guidance_from_springboot(
    request: SpringBootGuidanceRequest,
    authorization: Optional[str] = Header(None, description="Bearer JWT token"),
):
    """
    Auto-fetch student data from Spring Boot, combine with survey data
    from frontend, then run both guidance flows.
    """
    # Extract JWT token
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail="Token xác thực không hợp lệ. Vui lòng đăng nhập lại.",
        )

    token = authorization.replace("Bearer ", "")

    try:
        # ── Fetch data from Spring Boot ──
        # 1. Get student scores
        raw_scores = get_student_scores(token)
        aggregated = aggregate_subject_scores(raw_scores)
        score_fields = map_scores_to_schema(aggregated)

        # Validate we have all required subjects
        required_fields = [
            "math_score", "history_score", "physics_score",
            "chemistry_score", "biology_score", "english_score",
            "geography_score",
        ]
        missing = [f for f in required_fields if f not in score_fields]
        if missing:
            logger.warning("Missing subject scores: %s", missing)
            # Fill missing with 0.0
            for f in missing:
                score_fields[f] = 0.0

        scores = StudentScores(**score_fields)

        # 2. Get user profile (for gender)
        user_data = get_current_user(token)
        # gender in user-service is a boolean: true=male, false=female
        gender_bool = user_data.get("gender", True)
        gender = "nam" if gender_bool else "nữ"

        # Absences: default to 0 (can be extended to call attendance-service)
        profile = StudentProfile(gender=gender, absences=0)

        # ── Run both guidance flows ──
        holland_result = gemini_service.get_holland_guidance(
            request.holland_codes
        )
        academic_result = gemini_service.get_academic_guidance(
            scores=scores,
            profile=profile,
            survey=request.survey,
        )

        session_id = chat_manager.create_session(
            initial_context={
                "holland_result": holland_result,
                "academic_result": academic_result,
            }
        )

        return FullGuidanceResponse(
            holland_result=holland_result,
            academic_result=academic_result,
            session_id=session_id,
        )

    except SpringBootApiError as e:
        status = e.status_code
        if status == 401 or status == 403:
            raise HTTPException(status_code=401, detail="Phiên đăng nhập đã hết hạn.")
        raise HTTPException(status_code=status, detail=e.message)

    except SpringBootConnectionError as e:
        raise HTTPException(status_code=503, detail=e.message)

    except gemini_service.GeminiApiError as e:
        raise HTTPException(status_code=500, detail=e.message)

    except Exception as e:
        logger.error("Unexpected error in springboot guidance: %s", str(e))
        raise HTTPException(
            status_code=500,
            detail="Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
        )
