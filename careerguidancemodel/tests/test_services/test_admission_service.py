import pytest
import mongomock
from app.services import admission_service

@pytest.fixture
def mock_mongo(mocker):
    # Use mongomock to simulate MongoDB in memory
    mock_client = mongomock.MongoClient()
    mocker.patch("app.services.admission_service.MongoClient", return_value=mock_client)
    return mock_client

def test_extract_major_from_message():
    assert admission_service.extract_major_from_message("Điểm chuẩn ngành Công nghệ thông tin?") == "Công nghệ thông tin"
    assert admission_service.extract_major_from_message("cho em hỏi điểm chuẩn y khoa") == "y khoa"
    assert admission_service.extract_major_from_message("diem chuan nganh ke toan") == "ke toan"
    assert admission_service.extract_major_from_message("chào bạn") is None

def test_search_admission_scores_success(mock_mongo, mocker):
    mocker.patch("app.services.admission_service.auto_seed")
    admission_service.initialize()
    
    # Insert mock data
    db = admission_service._db
    db.admission_scores.insert_many([
        {
            "Trường": "Điểm chuẩn năm 2024 - Đại học Bách Khoa",
            "Ngành Tiếng Việt": "Công nghệ thông tin",
            "Phương thức": "Xét điểm thi THPT",
            "Điểm chuẩn": "28.5",
            "Tổ hợp": "A00, A01"
        },
        {
            "Trường": "Đại học Quốc Gia",
            "Ngành Tiếng Việt": "Công nghệ thông tin (Chất lượng cao)",
            "Phương thức": "ĐGNL",
            "Điểm chuẩn": "900",
            "Tổ hợp": ""
        }
    ])
    
    result = admission_service.search_admission_scores("Công nghệ thông tin")
    
    assert result is not None
    assert "Đại học Bách Khoa" in result
    assert "Đại học Quốc Gia" in result
    assert "28.5" in result
    assert "900" in result
    assert "Xét điểm thi THPT" in result

def test_search_admission_scores_not_found(mock_mongo):
    admission_service.initialize()
    
    result = admission_service.search_admission_scores("Ngành không tồn tại")
    
    assert result is None

def test_search_admission_scores_empty_query():
    result = admission_service.search_admission_scores("   ")
    assert result is None

def test_auto_seed_no_file(mocker, mock_mongo):
    mocker.patch("os.path.exists", return_value=False)
    admission_service.initialize()
    
    # It should log warning and not crash
    admission_service.auto_seed()
    
    count = admission_service._db.admission_scores.count_documents({})
    assert count == 0
