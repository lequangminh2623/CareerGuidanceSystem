"""
Prompt construction utilities.
Builds prompts that exactly match the fine-tuned model's training format
from fine_tune.jsonl.
"""

from app.models.schemas import (
    HollandResult,
    StudentScores,
    StudentProfile,
    StudentSurvey,
)


def build_holland_prompt(holland: HollandResult) -> str:
    """
    Build a Holland-based career guidance prompt.
    
    Matches the exact format from fine_tune.jsonl:
    "Tôi có kết quả trắc nghiệm Holland với 3 mã cao nhất là: I, S, A. 
     Nghề nghiệp nào phù hợp với tôi?"
    """
    return (
        f"Tôi có kết quả trắc nghiệm Holland với 3 mã cao nhất là: "
        f"{holland.code1.value}, {holland.code2.value}, {holland.code3.value}. "
        f"Nghề nghiệp nào phù hợp với tôi?"
    )


def build_academic_prompt(
    scores: StudentScores,
    profile: StudentProfile,
    survey: StudentSurvey,
) -> str:
    """
    Build an academic analysis career guidance prompt.
    
    Matches the exact format from fine_tune.jsonl:
    "Thông tin học sinh: Giới tính: nữ, Số ngày nghỉ: 0, 
     Điểm số: [Toán: 9.2, Sử: 8.2, Lý: 6.3, Hóa: 6.1, Sinh: 6.6, Anh: 8.4, Địa: 9.8]. 
     Kết quả trắc nghiệm: Làm thêm: không, Ngoại khóa: có, Giờ tự học/tuần: 29. 
     Nghề nghiệp nào phù hợp?"
    """
    part_time = "có" if survey.has_part_time_job else "không"
    extracurricular = "có" if survey.extracurricular_activities else "không"

    return (
        f"Thông tin học sinh: "
        f"Giới tính: {profile.gender}, "
        f"Số ngày nghỉ: {profile.absences}, "
        f"Điểm số: ["
        f"Toán: {scores.math_score}, "
        f"Sử: {scores.history_score}, "
        f"Lý: {scores.physics_score}, "
        f"Hóa: {scores.chemistry_score}, "
        f"Sinh: {scores.biology_score}, "
        f"Anh: {scores.english_score}, "
        f"Địa: {scores.geography_score}]. "
        f"Kết quả trắc nghiệm: "
        f"Làm thêm: {part_time}, "
        f"Ngoại khóa: {extracurricular}, "
        f"Giờ tự học/tuần: {survey.self_study_hours}. "
        f"Nghề nghiệp nào phù hợp?"
    )
