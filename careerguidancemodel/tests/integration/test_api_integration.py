import pytest
import os
from fastapi.testclient import TestClient
from app.main import app
from app.services import admission_service, chat_manager

client = TestClient(app)

@pytest.mark.integration
def test_chat_integration_mongo(mocker):
    # This test hits the /api/chat endpoint.
    # It ensures the router correctly connects to the Real Mongo Container
    # (via admission_service) when asking about "Điểm chuẩn".
    
    # We still mock Gemini because we don't want to hit the real Vertex AI in standard integration tests.
    is_real_ai = os.environ.get("RUN_REAL_AI_TESTS") == "1"
    
    if not is_real_ai:
        mocker.patch("app.routers.chat.gemini_service.chat_followup", return_value="Mocked Gemini Reply")
    
    # Ensure there's a specific record in MongoDB
    db = admission_service._db
    db.admission_scores.insert_one({
        "Trường": "HUTECH Integration",
        "Ngành Tiếng Việt": "Kỹ thuật Test API",
        "Phương thức": "Xét THPT",
        "Điểm chuẩn": "24.5",
        "Tổ hợp": "A00",
        "Ghi chú": ""
    })
    
    # Create a real chat session
    session_id = chat_manager.create_session(initial_context={"holland_result": "dummy"})
    
    # 1. Ask about admission scores (should hit Mongo)
    response_mongo = client.post("/api/chat", json={
        "session_id": session_id,
        "message": "Điểm chuẩn ngành Kỹ thuật Test API?"
    })
    
    assert response_mongo.status_code == 200
    reply = response_mongo.json()["reply"]
    
    assert "HUTECH Integration" in reply
    assert "24.5" in reply
    
    # 2. Ask a general question (should hit Gemini, mocked or real)
    response_gemini = client.post("/api/chat", json={
        "session_id": session_id,
        "message": "Nghề này làm gì?"
    })
    
    assert response_gemini.status_code == 200
    if not is_real_ai:
        assert response_gemini.json()["reply"] == "Mocked Gemini Reply"
