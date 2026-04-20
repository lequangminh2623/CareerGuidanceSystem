"""
HTTP client for Spring Boot microservices.
Forwards the user's JWT token to the API Gateway to fetch student data.
"""

import logging
import requests
from typing import Dict, Any, Optional, List
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

from app.config import settings

logger = logging.getLogger(__name__)

# ══════════════════════════════════════════════
#  Custom Exceptions
# ══════════════════════════════════════════════


class SpringBootApiError(Exception):
    """Raised when the Spring Boot API returns an error."""

    def __init__(self, status_code: int, message: str):
        self.status_code = status_code
        self.message = message
        super().__init__(f"Spring Boot API Error [{status_code}]: {message}")


class SpringBootConnectionError(Exception):
    """Raised when unable to connect to Spring Boot services."""

    def __init__(self, message: str = "Không thể kết nối đến hệ thống quản lý học sinh"):
        self.message = message
        super().__init__(message)


# ══════════════════════════════════════════════
#  HTTP Session with Retry
# ══════════════════════════════════════════════


def _build_session() -> requests.Session:
    """Build a requests Session with retry strategy."""
    session = requests.Session()
    retry_strategy = Retry(
        total=2,
        backoff_factor=0.5,
        status_forcelist=[502, 503, 504],
        allowed_methods=["GET"],
    )
    adapter = HTTPAdapter(max_retries=retry_strategy)
    session.mount("http://", adapter)
    session.mount("https://", adapter)
    return session


_session = _build_session()
_TIMEOUT = 10  # seconds


# ══════════════════════════════════════════════
#  API Methods
# ══════════════════════════════════════════════


def _make_request(
    method: str,
    path: str,
    token: str,
    params: Optional[Dict[str, str]] = None,
) -> Any:
    """
    Generic request method that forwards JWT to the API Gateway.

    Args:
        method: HTTP method (GET/POST)
        path: API path (e.g., /api/secure/scores/current-student)
        token: JWT Bearer token from the frontend
        params: Optional query parameters

    Returns:
        Parsed JSON response

    Raises:
        SpringBootApiError: If the API returns a non-2xx status
        SpringBootConnectionError: If the connection fails
    """
    url = f"{settings.SPRING_BOOT_BASE_URL}{path}"
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json",
    }

    try:
        response = _session.request(
            method=method,
            url=url,
            headers=headers,
            params=params,
            timeout=_TIMEOUT,
        )
        response.raise_for_status()
        return response.json()

    except requests.exceptions.HTTPError as e:
        status = e.response.status_code if e.response is not None else 500
        body = ""
        if e.response is not None:
            try:
                body = e.response.json().get("detail", e.response.text)
            except (ValueError, AttributeError):
                body = e.response.text

        logger.error("Spring Boot API error [%d] %s: %s", status, path, body)
        raise SpringBootApiError(status_code=status, message=str(body))

    except requests.exceptions.ConnectionError:
        logger.error("Cannot connect to Spring Boot at %s", url)
        raise SpringBootConnectionError()

    except requests.exceptions.Timeout:
        logger.error("Timeout connecting to Spring Boot at %s", url)
        raise SpringBootConnectionError(
            "Hệ thống quản lý học sinh phản hồi quá chậm"
        )

    except requests.exceptions.RequestException as e:
        logger.error("Unexpected request error: %s", str(e))
        raise SpringBootConnectionError(str(e))


def get_student_scores(token: str) -> List[Dict[str, Any]]:
    """
    Fetch the current student's scores from the score-service.

    Calls: GET /api/secure/scores/current-student
    Returns a list of StudentScoreResponseDTO objects.
    """
    return _make_request("GET", "/api/secure/scores/current-student", token)


def get_student_statistics(token: str) -> Dict[str, Any]:
    """
    Fetch the current student's statistics (semester & year averages).

    Calls: GET /api/secure/statistics/student
    Returns StudentStatisticsResponseDTO.
    """
    return _make_request("GET", "/api/secure/statistics/student", token)


def get_current_user(token: str) -> Dict[str, Any]:
    """
    Fetch the current user's profile from the user-service.

    Calls: GET /api/secure/users/current
    Returns UserDetailsResponseDTO with gender, name, etc.
    """
    return _make_request("GET", "/api/secure/users/current", token)


def aggregate_subject_scores(
    raw_scores: List[Dict[str, Any]],
) -> Dict[str, float]:
    """
    Aggregate raw per-section scores into per-subject averages.
    """
    from collections import defaultdict

    subject_scores: Dict[str, List[float]] = defaultdict(list)

    for score_entry in raw_scores:
        subject_name = score_entry.get("subjectName", "").strip()
        midterm = score_entry.get("midtermScore")
        final = score_entry.get("finalScore")

        # More robust average: if one is missing, use the other. 
        # If both present, take average.
        if midterm is not None and final is not None:
            avg = round((midterm + final) / 2, 1)
            subject_scores[subject_name].append(avg)
        elif midterm is not None:
            subject_scores[subject_name].append(float(midterm))
        elif final is not None:
            subject_scores[subject_name].append(float(final))

    # Take the average across all sections/semesters found for each subject
    result: Dict[str, float] = {}
    for subject, scores_list in subject_scores.items():
        if scores_list:
            result[subject] = round(sum(scores_list) / len(scores_list), 1)

    return result


# Subject name mapping: Vietnamese -> English field name
SUBJECT_MAP = {
    # English names (from your screenshots)
    "Math": "math_score",
    "MATH": "math_score",
    "Physics": "physics_score",
    "PHYSICS": "physics_score",
    "Chemistry": "chemistry_score",
    "CHEMISTRY": "chemistry_score",
    "Biology": "biology_score",
    "BIOLOGY": "biology_score",
    "Geography": "geography_score",
    "GEOGRAPHY": "geography_score",
    "History": "history_score",
    "HISTORY": "history_score",
    "English": "english_score",
    "ENGLISH": "english_score",
    
    # Vietnamese names
    "Toán học": "math_score",
    "Toán": "math_score",
    "Lịch sử": "history_score",
    "Sử": "history_score",
    "Vật lý": "physics_score",
    "Vật lí": "physics_score",
    "Lý": "physics_score",
    "Hóa học": "chemistry_score",
    "Hóa": "chemistry_score",
    "Sinh học": "biology_score",
    "Sinh": "biology_score",
    "Tiếng Anh": "english_score",
    "Anh": "english_score",
    "Địa lý": "geography_score",
    "Địa": "geography_score",
}


def map_scores_to_schema(aggregated: Dict[str, float]) -> Dict[str, float]:
    """
    Map aggregated per-subject scores (Vietnamese names) to schema field names.

    Returns dict with keys: math_score, history_score, physics_score,
    chemistry_score, biology_score, english_score, geography_score.
    """
    result: Dict[str, float] = {}
    for vn_name, field_name in SUBJECT_MAP.items():
        if vn_name in aggregated and field_name not in result:
            result[field_name] = aggregated[vn_name]

    return result
