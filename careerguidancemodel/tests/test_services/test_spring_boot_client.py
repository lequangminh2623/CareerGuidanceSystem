import pytest
import respx
from httpx import Response
from app.services import spring_boot_client
from app.config import settings

def test_aggregate_subject_scores():
    raw_scores = [
        {"subjectName": "Toán học", "midtermScore": 8.0, "finalScore": 9.0},
        {"subjectName": "Vật lý", "midtermScore": 7.0, "finalScore": 8.0},
        {"subjectName": "Hóa học", "midtermScore": 8.0}, # Only midterm
        {"subjectName": "Sinh học", "finalScore": 7.5}, # Only final
    ]
    
    aggregated = spring_boot_client.aggregate_subject_scores(raw_scores)
    
    assert aggregated["Toán học"] == 8.5
    assert aggregated["Vật lý"] == 7.5
    assert aggregated["Hóa học"] == 8.0
    assert aggregated["Sinh học"] == 7.5

def test_map_scores_to_schema():
    aggregated = {
        "Toán học": 8.5,
        "Vật lý": 7.5,
        "Lịch sử": 6.0,
        "Invalid Subject": 10.0
    }
    
    mapped = spring_boot_client.map_scores_to_schema(aggregated)
    
    assert mapped["math_score"] == 8.5
    assert mapped["physics_score"] == 7.5
    assert mapped["history_score"] == 6.0
    assert "Invalid Subject" not in mapped

def test_get_student_scores_success(mocker):
    token = "valid_token"
    mock_response = mocker.Mock()
    mock_response.json.return_value = [{"subjectName": "Toán", "midtermScore": 8.0, "finalScore": 9.0}]
    mock_response.raise_for_status.return_value = None
    mocker.patch("app.services.spring_boot_client._session.request", return_value=mock_response)
    
    result = spring_boot_client.get_student_scores(token)
    assert result == [{"subjectName": "Toán", "midtermScore": 8.0, "finalScore": 9.0}]

def test_get_student_scores_unauthorized(mocker):
    token = "invalid_token"
    
    # Mock the HTTPError
    mock_response = mocker.Mock()
    mock_response.status_code = 401
    mock_response.json.return_value = {"detail": "Unauthorized"}
    
    from requests.exceptions import HTTPError
    error = HTTPError("Unauthorized")
    error.response = mock_response
    
    mocker.patch("app.services.spring_boot_client._session.request", side_effect=error)
    
    with pytest.raises(spring_boot_client.SpringBootApiError) as exc_info:
        spring_boot_client.get_student_scores(token)
        
    assert exc_info.value.status_code == 401

@respx.mock
def test_make_request_timeout(mocker):
    # Simulate a timeout using side_effect on the requests session
    mocker.patch("app.services.spring_boot_client._session.request", side_effect=spring_boot_client.requests.exceptions.Timeout)
    
    with pytest.raises(spring_boot_client.SpringBootConnectionError, match="phản hồi quá chậm"):
        spring_boot_client._make_request("GET", "/api/test", "token")
