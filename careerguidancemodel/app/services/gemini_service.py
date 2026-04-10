"""
Gemini API integration service.
Handles both fine-tuned model calls and follow-up chat with system instruction.
"""

import logging
from typing import List, Dict, Optional

import google.generativeai as genai

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
    Initialize Gemini API with the API key.
    Must be called on application startup.
    """
    global _finetuned_model, _chat_model

    genai.configure(api_key=settings.GEMINI_API_KEY)

    # Fine-tuned model for career guidance (Flow 1 & Flow 2)
    _finetuned_model = genai.GenerativeModel(
        model_name=settings.GEMINI_MODEL_NAME
    )

    # General model for follow-up chat with system instruction
    _chat_model = genai.GenerativeModel(
        model_name="gemini-2.0-flash",
        system_instruction=CAREER_COUNSELOR_INSTRUCTION,
    )

    logger.info(
        "Gemini API initialized: fine-tuned=%s, chat=gemini-2.0-flash",
        settings.GEMINI_MODEL_NAME,
    )


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


def chat_followup(
    message: str,
    history: List[Dict[str, str]],
    initial_context: Optional[Dict[str, str]] = None,
) -> str:
    """
    Handle a follow-up chat message using the general Gemini model
    with career counselor system instruction.

    Args:
        message: The user's new message
        history: Previous conversation history as list of
                 {"role": "user"/"model", "content": "..."}
        initial_context: Optional dict with "holland_result" and/or
                        "academic_result" to include as context

    Returns:
        The model's reply text
    """
    if _chat_model is None:
        raise GeminiApiError("Gemini chưa được khởi tạo")

    try:
        # Build the conversation history for Gemini
        gemini_history = []

        # Inject the initial guidance results as context if this is early in the chat
        if initial_context and len(history) == 0:
            context_parts = []
            if "holland_result" in initial_context:
                context_parts.append(
                    f"Kết quả tư vấn Holland: {initial_context['holland_result']}"
                )
            if "academic_result" in initial_context:
                context_parts.append(
                    f"Kết quả phân tích học tập: {initial_context['academic_result']}"
                )

            if context_parts:
                context_msg = (
                    "Dưới đây là kết quả tư vấn ban đầu của học sinh:\n\n"
                    + "\n\n".join(context_parts)
                    + "\n\nHãy sử dụng thông tin này làm bối cảnh để trả lời "
                    "các câu hỏi tiếp theo của học sinh."
                )
                gemini_history.append(
                    {"role": "user", "parts": [context_msg]}
                )
                gemini_history.append(
                    {
                        "role": "model",
                        "parts": [
                            "Tôi đã nhận được kết quả tư vấn ban đầu. "
                            "Tôi sẽ sử dụng thông tin này để hỗ trợ em tốt hơn. "
                            "Em có câu hỏi gì thêm không?"
                        ],
                    }
                )

        # Add previous conversation messages
        for msg in history:
            gemini_history.append(
                {"role": msg["role"], "parts": [msg["content"]]}
            )

        # Start chat with history
        chat = _chat_model.start_chat(history=gemini_history)
        response = chat.send_message(message)
        result = response.text.strip()

        logger.info("Chat reply: %s", result[:200])
        return result

    except Exception as e:
        logger.error("Gemini Chat API error: %s", str(e))
        raise GeminiApiError(f"Lỗi khi gọi Gemini API: {str(e)}")
