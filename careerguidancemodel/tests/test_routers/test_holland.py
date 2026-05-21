import pytest
from app.services import gemini_service

def test_holland_guidance_cache_miss(client, mocker):
    mocker.patch("app.routers.holland.get_cached", return_value=None)
    mock_set_cached = mocker.patch("app.routers.holland.set_cached")
    mocker.patch("app.routers.holland.gemini_service.get_holland_guidance", return_value="Holland Analysis")
    mocker.patch("app.routers.holland.chat_manager.create_session", return_value="new_session")
    
    response = client.post("/api/holland/guidance", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"}
    })
    
    assert response.status_code == 200
    assert response.json()["holland_result"] == "Holland Analysis"
    assert response.json()["session_id"] == "new_session"
    mock_set_cached.assert_called_once()

def test_holland_guidance_cache_hit(client, mocker):
    mocker.patch("app.routers.holland.get_cached", return_value="Cached Holland Analysis")
    mock_gemini = mocker.patch("app.routers.holland.gemini_service.get_holland_guidance")
    mocker.patch("app.routers.holland.chat_manager.create_session", return_value="new_session")
    
    response = client.post("/api/holland/guidance", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"}
    })
    
    assert response.status_code == 200
    assert response.json()["holland_result"] == "Cached Holland Analysis"
    mock_gemini.assert_not_called()

def test_holland_guidance_gemini_error(client, mocker):
    mocker.patch("app.routers.holland.get_cached", return_value=None)
    mocker.patch("app.routers.holland.gemini_service.get_holland_guidance", side_effect=gemini_service.GeminiApiError("Error API"))
    
    response = client.post("/api/holland/guidance", json={
        "holland_codes": {"code1": "R", "code2": "I", "code3": "A"}
    })
    
    assert response.status_code == 500
    assert "Error API" in response.json()["message"]
