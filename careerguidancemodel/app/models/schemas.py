"""
Pydantic schemas for request/response models.
All field names and formats match the fine-tuned model's training data.
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from enum import Enum


# ══════════════════════════════════════════════
#  Enums
# ══════════════════════════════════════════════

class HollandCode(str, Enum):
    """RIASEC Holland codes."""
    R = "R"  # Realistic
    I = "I"  # Investigative
    A = "A"  # Artistic
    S = "S"  # Social
    E = "E"  # Enterprising
    C = "C"  # Conventional


# ══════════════════════════════════════════════
#  Request Schemas
# ══════════════════════════════════════════════

class HollandResult(BaseModel):
    """Top 3 Holland codes from the RIASEC test."""
    code1: HollandCode = Field(..., description="Mã Holland cao nhất (R/I/A/S/E/C)")
    code2: HollandCode = Field(..., description="Mã Holland cao thứ 2")
    code3: HollandCode = Field(..., description="Mã Holland cao thứ 3")


class StudentScores(BaseModel):
    """Academic scores for 7 subjects."""
    math_score: float = Field(..., ge=0, le=10, description="Điểm Toán")
    history_score: float = Field(..., ge=0, le=10, description="Điểm Sử")
    physics_score: float = Field(..., ge=0, le=10, description="Điểm Lý")
    chemistry_score: float = Field(..., ge=0, le=10, description="Điểm Hóa")
    biology_score: float = Field(..., ge=0, le=10, description="Điểm Sinh")
    english_score: float = Field(..., ge=0, le=10, description="Điểm Anh")
    geography_score: float = Field(..., ge=0, le=10, description="Điểm Địa")


class StudentProfile(BaseModel):
    """Basic student profile info."""
    gender: str = Field(..., pattern=r"^(nam|nữ)$", description="Giới tính: nam hoặc nữ")
    absences: int = Field(..., ge=0, description="Số ngày nghỉ")


class StudentSurvey(BaseModel):
    """Additional survey data from frontend form."""
    has_part_time_job: bool = Field(..., description="Có làm thêm không")
    extracurricular_activities: bool = Field(..., description="Có tham gia ngoại khóa không")
    self_study_hours: int = Field(..., ge=0, le=100, description="Số giờ tự học/tuần")


class HollandGuidanceRequest(BaseModel):
    """Request body for Holland-based career guidance (Flow 1)."""
    holland_codes: HollandResult


class AcademicGuidanceRequest(BaseModel):
    """Request body for academic analysis career guidance (Flow 2)."""
    scores: StudentScores
    profile: StudentProfile
    survey: StudentSurvey


class FullGuidanceRequest(BaseModel):
    """Request body for combined guidance (both flows)."""
    holland_codes: HollandResult
    scores: StudentScores
    profile: StudentProfile
    survey: StudentSurvey


class SpringBootGuidanceRequest(BaseModel):
    """Request body for guidance using Spring Boot data + survey from frontend."""
    survey: StudentSurvey
    holland_codes: HollandResult


class ChatMessageRequest(BaseModel):
    """Request body for follow-up chat."""
    message: str = Field(..., min_length=1, max_length=2000, description="Tin nhắn")
    session_id: str = Field(..., description="ID phiên hội thoại")


# ══════════════════════════════════════════════
#  Response Schemas
# ══════════════════════════════════════════════

class HollandGuidanceResponse(BaseModel):
    """Response for Holland-based guidance."""
    holland_result: str
    session_id: str


class AcademicGuidanceResponse(BaseModel):
    """Response for academic guidance."""
    academic_result: str
    session_id: str


class FullGuidanceResponse(BaseModel):
    """Response for combined guidance (both flows)."""
    holland_result: str
    academic_result: str
    session_id: str


class ChatMessageResponse(BaseModel):
    """Response for follow-up chat."""
    reply: str
    session_id: str


class ChatHistoryMessage(BaseModel):
    """A single message in chat history."""
    role: str  # "user" or "model"
    content: str


class ChatHistoryResponse(BaseModel):
    """Full chat history for a session."""
    session_id: str
    messages: List[ChatHistoryMessage]


class ErrorResponse(BaseModel):
    """Standard error response."""
    detail: str


class HealthResponse(BaseModel):
    """Health check response."""
    status: str
    service: str
    version: str
