import pytest
import json
from app.services import gemini_service, spring_boot_client

def test_academic_guidance_cache_miss(client, mocker):
    mocker.patch("app.routers.guidance.get_cached", return_value=None)
    mock_set_cached = mocker.patch("app.routers.guidance.set_cached")
    mocker.patch("app.routers.guidance.gemini_service.get_academic_guidance", return_value="Academic Analysis")
    mocker.patch("app.routers.guidance.chat_manager.create_session", return_value="new_session")
    
    response = client.post("/api/guidance/academic", json={
        "scores": {
            "math_score": 8, "history_score": 7, "physics_score": 8,
            "chemistry_score": 8, "biology_score": 7, "english_score": 8, "geography_score": 7
        },
        "profile": {"gender": "nam", "absences": 0},
        "survey": {"has_part_time_job": False, "extracurricular_activities": True, "self_study_hours": 10}
    })
    
    assert response.status_code == 200
    assert response.json()["academic_result"] == "Academic Analysis"
    assert response.json()["session_id"] == "new_session"
    mock_set_cached.assert_called_once()

def test_full_guidance_cache_hit(client, mocker):
    cached_data = json.dumps({
        "holland_result": "Holland Analysis",
        "academic_result": "Academic Analysis"
    })
    mocker.patch("app.routers.guidance.get_cached", return_value=cached_data)
    mock_gemini_holland = mocker.patch("app.routers.guidance.gemini_service.get_holland_guidance")
    mocker.patch("app.routers.guidance.chat_manager.create_session", return_value="new_session")
    
    response = client.post("/api/guidance/full", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"},
        "scores": {
            "math_score": 8, "history_score": 7, "physics_score": 8,
            "chemistry_score": 8, "biology_score": 7, "english_score": 8, "geography_score": 7
        },
        "profile": {"gender": "nam", "absences": 0},
        "survey": {"has_part_time_job": False, "extracurricular_activities": True, "self_study_hours": 10}
    })
    
    assert response.status_code == 200
    assert response.json()["holland_result"] == "Holland Analysis"
    assert response.json()["academic_result"] == "Academic Analysis"
    # Should not call API if cache hits
    mock_gemini_holland.assert_not_called()

def test_guidance_from_springboot_success(client, mocker):
    mocker.patch("app.routers.guidance.get_student_scores", return_value=[])
    mocker.patch("app.routers.guidance.aggregate_subject_scores", return_value={})
    mocker.patch("app.routers.guidance.map_scores_to_schema", return_value={
        "math_score": 8, "history_score": 7, "physics_score": 8,
        "chemistry_score": 8, "biology_score": 7, "english_score": 8, "geography_score": 7
    })
    mocker.patch("app.routers.guidance.get_current_user", return_value={"gender": True})
    
    mocker.patch("app.routers.guidance.get_cached", return_value=None)
    mocker.patch("app.routers.guidance.gemini_service.get_holland_guidance", return_value="Holland")
    mocker.patch("app.routers.guidance.gemini_service.get_academic_guidance", return_value="Academic")
    mocker.patch("app.routers.guidance.chat_manager.create_session", return_value="session")
    
    response = client.post("/api/guidance/from-springboot", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"},
        "survey": {"has_part_time_job": False, "extracurricular_activities": True, "self_study_hours": 10}
    }, headers={"Authorization": "Bearer valid_token"})
    
    assert response.status_code == 200
    assert response.json()["holland_result"] == "Holland"
    assert response.json()["academic_result"] == "Academic"

def test_guidance_from_springboot_no_token(client):
    response = client.post("/api/guidance/from-springboot", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"},
        "survey": {"has_part_time_job": False, "extracurricular_activities": True, "self_study_hours": 10}
    })
    
    assert response.status_code == 401
    assert "Token xác thực không hợp lệ" in response.json()["message"]
