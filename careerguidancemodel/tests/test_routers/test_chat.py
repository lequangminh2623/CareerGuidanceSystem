import pytest
from unittest.mock import ANY

def test_send_chat_message_success_gemini(client, mocker):
    mocker.patch("app.routers.chat.chat_manager.get_session", return_value={"initial_context": {}})
    mocker.patch("app.routers.chat.chat_manager.get_history", return_value=[])
    mocker.patch("app.routers.chat.chat_manager.get_initial_context", return_value={})
    mocker.patch("app.routers.chat.admission_service.extract_major_from_message", return_value=None)
    mocker.patch("app.routers.chat.gemini_service.chat_followup", return_value="Gemini response")
    mock_add = mocker.patch("app.routers.chat.chat_manager.add_message")
    
    response = client.post("/api/chat", json={
        "session_id": "test_session",
        "message": "Hello"
    })
    
    assert response.status_code == 200
    assert response.json()["reply"] == "Gemini response"
    assert mock_add.call_count == 2 # Added user message and model reply

def test_send_chat_message_admission_score(client, mocker):
    mocker.patch("app.routers.chat.chat_manager.get_session", return_value={"initial_context": {}})
    mocker.patch("app.routers.chat.admission_service.extract_major_from_message", return_value="Công nghệ thông tin")
    mocker.patch("app.routers.chat.admission_service.search_admission_scores", return_value="Điểm chuẩn CNTT: 28")
    mock_add = mocker.patch("app.routers.chat.chat_manager.add_message")
    
    response = client.post("/api/chat", json={
        "session_id": "test_session",
        "message": "Điểm chuẩn ngành công nghệ thông tin?"
    })
    
    assert response.status_code == 200
    assert response.json()["reply"] == "Điểm chuẩn CNTT: 28"
    assert mock_add.call_count == 2

def test_send_chat_message_session_not_found(client, mocker):
    mocker.patch("app.routers.chat.chat_manager.get_session", return_value=None)
    
    response = client.post("/api/chat", json={
        "session_id": "invalid",
        "message": "Hello"
    })
    
    assert response.status_code == 404
    assert "không tồn tại" in response.json()["message"]

def test_get_chat_history_success(client, mocker):
    history_data = [
        {"role": "user", "content": "Hi"},
        {"role": "model", "content": "Hello"}
    ]
    mocker.patch("app.routers.chat.chat_manager.get_history", return_value=history_data)
    
    response = client.get("/api/chat/test_session/history")
    
    assert response.status_code == 200
    assert len(response.json()["messages"]) == 2
    assert response.json()["messages"][0]["role"] == "user"

def test_get_chat_history_not_found(client, mocker):
    mocker.patch("app.routers.chat.chat_manager.get_history", return_value=None)
    
    response = client.get("/api/chat/invalid_session/history")
    
    assert response.status_code == 404
