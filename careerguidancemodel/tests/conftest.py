import pytest
from fastapi.testclient import TestClient
from app.main import app

@pytest.fixture
def client():
    """Provides a TestClient for testing FastAPI endpoints."""
    return TestClient(app)

@pytest.fixture
def mock_gemini(mocker):
    """Mocks the gemini_service calls."""
    mocker.patch("app.services.gemini_service.initialize", return_value=None)
    return mocker

@pytest.fixture
def mock_redis(mocker):
    """Mocks the redis_cache service."""
    mocker.patch("app.services.redis_cache.initialize", return_value=None)
    mocker.patch("app.services.redis_cache.get_cached", return_value=None)
    mocker.patch("app.services.redis_cache.set_cached", return_value=None)
    return mocker

@pytest.fixture
def mock_mongodb(mocker):
    """Mocks the admission_service calls to MongoDB."""
    mocker.patch("app.services.admission_service.initialize", return_value=None)
    return mocker
