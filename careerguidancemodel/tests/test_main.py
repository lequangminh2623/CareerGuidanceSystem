import pytest
from app.main import app, http_exception_handler, general_exception_handler, validation_exception_handler
from fastapi.testclient import TestClient
from starlette.exceptions import HTTPException as StarletteHTTPException
from fastapi.exceptions import RequestValidationError
from fastapi import Request
import json
import asyncio

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"

def test_session_count(mocker):
    mocker.patch("app.main.chat_manager.get_active_session_count", return_value=5)
    response = client.get("/health/sessions")
    assert response.status_code == 200
    assert response.json()["active_sessions"] == 5

def test_validation_exception_handler():
    # Sending invalid data (missing required fields) to trigger 422 Validation Error
    response = client.post("/api/holland/guidance", json={})
    
    assert response.status_code == 400
    data = response.json()
    assert data["status"] == 400
    assert data["error"] == "Validation Error"
    assert "Dữ liệu gửi lên không đúng định dạng" in data["message"]
    assert "details" in data
    assert "path" in data

@pytest.mark.asyncio
async def test_http_exception_handler_direct():
    req = Request(scope={"type": "http", "method": "GET", "path": "/test", "headers": []})
    exc = StarletteHTTPException(status_code=403, detail="Forbidden Error")
    
    response = await http_exception_handler(req, exc)
    
    assert response.status_code == 403
    data = json.loads(response.body)
    assert data["status"] == 403
    assert data["error"] == "HTTP Exception"
    assert "Forbidden Error" in data["message"]

@pytest.mark.asyncio
async def test_general_exception_handler_direct():
    req = Request(scope={"type": "http", "method": "GET", "path": "/test", "headers": []})
    exc = Exception("Unexpected System Error")
    
    response = await general_exception_handler(req, exc)
    
    assert response.status_code == 500
    data = json.loads(response.body)
    assert data["status"] == 500
    assert data["error"] == "Internal Server Error"
    assert "Đã có lỗi xảy ra từ máy chủ" in data["message"]
