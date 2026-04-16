"""
Gemini API integration service.
Handles both fine-tuned model calls and follow-up chat with system instruction.
"""

import logging
from typing import List, Dict, Optional

import google.generativeai as genai
import vertexai
from vertexai.generative_models import GenerativeModel as VertexGenerativeModel, Content, Part

# ... (Careful to keep other imports)

from app.config import settings
from app.models.schemas import (
    HollandResult,
    StudentScores,
    StudentProfile,
    StudentSurvey,
)
from app.utils.prompt_builder import build_holland_prompt, build_academic_prompt

logger = logging.getLogger(__name__)

# ══════════════════════════════════════════════
#  Custom Exception
# ══════════════════════════════════════════════


class GeminiApiError(Exception):
    """Raised when the Gemini API returns an error."""

    def __init__(self, message: str = "Lỗi từ dịch vụ AI Gemini"):
        self.message = message
        super().__init__(message)


# ══════════════════════════════════════════════
#  System Instructions
# ══════════════════════════════════════════════

CAREER_COUNSELOR_INSTRUCTION = (
    "Bạn là một chuyên gia tư vấn hướng nghiệp dành riêng cho học sinh "
    "trung học phổ thông tại Việt Nam. Nhiệm vụ của bạn là lắng nghe, "
    "phân tích dữ liệu học tập và sở thích để đưa ra những lời khuyên "
    "thực tế, truyền cảm hứng và phù hợp với xu hướng thị trường lao động. "
    "Ngôn ngữ giao tiếp: Thân thiện, chuyên nghiệp, dễ hiểu."
)


# ══════════════════════════════════════════════
#  Initialization
# ══════════════════════════════════════════════

_finetuned_model = None
_chat_model = None


def initialize():
    """
    Initialize Gemini API (Vertex AI).
    Must be called on application startup.
    Uses the SAME fine-tuned model for both initial guidance and follow-up chat.
    """
    global _finetuned_model, _chat_model

    # Initialize Vertex AI
    vertexai.init(project=settings.GOOGLE_CLOUD_PROJECT, location=settings.GOOGLE_CLOUD_REGION)
    
    # Resource name for the fine-tuned model endpoint
    model_resource_name = f"projects/{settings.GOOGLE_CLOUD_PROJECT}/locations/{settings.GOOGLE_CLOUD_REGION}/endpoints/{settings.GEMINI_MODEL_NAME}"

    # Initialize models using the fine-tuned endpoint
    # We use the same model for both to ensure consistent counseling logic.
    _finetuned_model = VertexGenerativeModel(model_resource_name)
    _chat_model = _finetuned_model

    logger.info("✓ Vertex AI initialized (using tuned model for ALL flows: %s)", model_resource_name)


# ══════════════════════════════════════════════
#  Flow 1: Holland-Based Guidance
# ══════════════════════════════════════════════


def get_holland_guidance(holland: HollandResult) -> str:
    """
    Get career guidance based on Holland RIASEC codes.
    Uses the fine-tuned model with the exact trained prompt format.

    Args:
        holland: The top 3 Holland codes

    Returns:
        Career recommendation text from the fine-tuned model
    """
    if _finetuned_model is None:
        raise GeminiApiError("Gemini chưa được khởi tạo")

    prompt = build_holland_prompt(holland)
    logger.info("Holland prompt: %s", prompt)

    try:
        response = _finetuned_model.generate_content(prompt)
        result = response.text.strip()
        logger.info("Holland result: %s", result[:200])
        return result

    except Exception as e:
        logger.error("Gemini Holland API error: %s", str(e))
        raise GeminiApiError(f"Lỗi khi gọi Gemini API: {str(e)}")


# ══════════════════════════════════════════════
#  Flow 2: Academic Analysis Guidance
# ══════════════════════════════════════════════


def get_academic_guidance(
    scores: StudentScores,
    profile: StudentProfile,
    survey: StudentSurvey,
) -> str:
    """
    Get career guidance based on academic scores and student profile.
    Uses the fine-tuned model with the exact trained prompt format.

    Args:
        scores: Academic scores for 7 subjects
        profile: Gender and absence info
        survey: Part-time job, extracurricular, self-study hours

    Returns:
        Career recommendation text from the fine-tuned model
    """
    if _finetuned_model is None:
        raise GeminiApiError("Gemini chưa được khởi tạo")

    prompt = build_academic_prompt(scores, profile, survey)
    logger.info("Academic prompt: %s", prompt)

    try:
        response = _finetuned_model.generate_content(prompt)
        result = response.text.strip()
        logger.info("Academic result: %s", result[:200])
        return result

    except Exception as e:
        logger.error("Gemini Academic API error: %s", str(e))
        raise GeminiApiError(f"Lỗi khi gọi Gemini API: {str(e)}")


# ══════════════════════════════════════════════
#  Follow-Up Chat
# ══════════════════════════════════════════════


# Max history messages to keep for context (5 pairs)
MAX_HISTORY_MESSAGES = 10


def chat_followup(
    message: str,
    history: List[Dict[str, str]],
    initial_context: Optional[Dict[str, str]] = None,
) -> str:
    """
    Handle a follow-up chat message using the general Gemini model
    with career counselor system instruction.
    """
    if _chat_model is None:
        raise GeminiApiError("Gemini chưa được khởi tạo")

    try:
        # Build the conversation history for Gemini
        gemini_history = []

        # 1. ALWAYS inject the initial guidance results as base context.
        # This ensures that even if history is truncated, the AI knows the user's profile.
        if initial_context:
            context_parts = []
            if "holland_result" in initial_context:
                # Truncate context if it's extremely long to save tokens
                res = initial_context["holland_result"]
                if len(res) > 1500:
                    res = res[:1400] + "... [nội dung đã được lược bớt để tiết kiệm bộ nhớ]"
                context_parts.append(f"Kết quả RIASEC: {res}")
            if "academic_result" in initial_context:
                res = initial_context["academic_result"]
                if len(res) > 1500:
                    res = res[:1400] + "... [nội dung đã được lược bớt để tiết kiệm bộ nhớ]"
                context_parts.append(f"Kết quả phân tích học tập: {res}")

            if context_parts:
                context_msg = (
                    "Dưới đây là bối cảnh ban đầu của học sinh (HÃY LUÔN GHI NHỚ THÔNG TIN NÀY):\n\n"
                    + "\n\n".join(context_parts)
                    + "\n\nHãy sử dụng thông tin này làm nền tảng để trả lời các câu hỏi."
                )
                gemini_history.append(
                    Content(role="user", parts=[Part.from_text(context_msg)])
                )
                gemini_history.append(
                    Content(
                        role="model",
                        parts=[Part.from_text("Tôi đã nắm được thông tin định hướng ban đầu. Tôi sẵn sàng trả lời các câu hỏi tiếp theo dựa trên bối cảnh này.")],
                    )
                )

        # 2. Add previous conversation messages (TRUNCATED to save tokens)
        truncated_history = history[-MAX_HISTORY_MESSAGES:] if len(history) > MAX_HISTORY_MESSAGES else history
        
        for msg in truncated_history:
            gemini_history.append(
                Content(role=msg["role"], parts=[Part.from_text(msg["content"])])
            )

        # Start chat with history
        chat = _chat_model.start_chat(history=gemini_history)
        response = chat.send_message(message)
        result = response.text.strip()

        logger.info("Chat reply (history size: %d): %s", len(truncated_history), result[:100])
        return result

    except Exception as e:
        logger.error("Gemini Chat API error: %s", str(e))
        raise GeminiApiError(f"Lỗi khi gọi Gemini API: {str(e)}")
