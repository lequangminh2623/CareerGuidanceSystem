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
    
    Matches the base format from fine_tune.jsonl but adds specific 
    instructions for formatting and explanation.
    """
    return (
        f"Tôi có kết quả trắc nghiệm Holland với 3 mã cao nhất là: "
        f"{holland.code1.value}, {holland.code2.value}, {holland.code3.value}. "
        f"Nghề nghiệp nào phù hợp với tôi?\n\n"
        f"Yêu cầu: Hãy chọn ra 5 nghề nghiệp phù hợp nhất và giải thích ngắn gọn "
        f"lý do tại sao hồ sơ này lại phù hợp với các mã Holland đó. "
        f"Hãy viết bằng giọng văn tư vấn thân thiện, không liệt kê khô khan."
    )


def build_academic_prompt(
    scores: StudentScores,
    profile: StudentProfile,
    survey: StudentSurvey,
) -> str:
    """
    Build an academic analysis career guidance prompt.
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
        f"Nghề nghiệp nào phù hợp?\n\n"
        f"Yêu cầu: Hãy chọn ra đúng 5 nghề phù hợp nhất dựa trên thế mạnh điểm số "
        f"và hồ sơ cá nhân trên. Với mỗi nghề, hãy giải thích ngắn gọn lý do tại sao "
        f"tôi nên theo đuổi (ví dụ: nhờ thế mạnh về tự nhiên hay kỹ năng xã hội). "
        f"Hãy viết bằng giọng văn tư vấn, truyền cảm hứng."
    )
