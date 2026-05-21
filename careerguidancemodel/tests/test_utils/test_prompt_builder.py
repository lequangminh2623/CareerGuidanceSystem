import pytest
from app.utils.prompt_builder import build_holland_prompt, build_academic_prompt
from app.models.schemas import HollandResult, HollandCode, StudentScores, StudentProfile, StudentSurvey

def test_build_holland_prompt():
    # Arrange
    holland = HollandResult(code1=HollandCode.R, code2=HollandCode.I, code3=HollandCode.A)

    # Act
    prompt = build_holland_prompt(holland)

    # Assert
    assert "Tôi có kết quả trắc nghiệm Holland với 3 mã cao nhất là: R, I, A." in prompt
    assert "Hãy chọn ra 5 nghề nghiệp phù hợp nhất" in prompt
    assert "Hãy viết bằng giọng văn tư vấn thân thiện" in prompt

def test_build_academic_prompt_with_activities():
    # Arrange
    scores = StudentScores(
        math_score=8.5, history_score=7.0, physics_score=9.0,
        chemistry_score=8.0, biology_score=6.5, english_score=7.5, geography_score=6.0
    )
    profile = StudentProfile(gender="nam", absences=2)
    survey = StudentSurvey(has_part_time_job=True, extracurricular_activities=True, self_study_hours=15)

    # Act
    prompt = build_academic_prompt(scores, profile, survey)

    # Assert
    assert "Giới tính: nam" in prompt
    assert "Số ngày nghỉ: 2" in prompt
    assert "Toán: 8.5" in prompt
    assert "Lý: 9.0" in prompt
    assert "Làm thêm: có" in prompt
    assert "Ngoại khóa: có" in prompt
    assert "Giờ tự học/tuần: 15" in prompt
    assert "Hãy chọn ra đúng 5 nghề phù hợp nhất" in prompt

def test_build_academic_prompt_without_activities():
    # Arrange
    scores = StudentScores(
        math_score=5.5, history_score=6.0, physics_score=5.0,
        chemistry_score=6.0, biology_score=7.5, english_score=5.5, geography_score=8.0
    )
    profile = StudentProfile(gender="nữ", absences=0)
    survey = StudentSurvey(has_part_time_job=False, extracurricular_activities=False, self_study_hours=5)

    # Act
    prompt = build_academic_prompt(scores, profile, survey)

    # Assert
    assert "Giới tính: nữ" in prompt
    assert "Số ngày nghỉ: 0" in prompt
    assert "Toán: 5.5" in prompt
    assert "Địa: 8.0" in prompt
    assert "Làm thêm: không" in prompt
    assert "Ngoại khóa: không" in prompt
    assert "Giờ tự học/tuần: 5" in prompt
