import pytest
import os
from app.services import admission_service

@pytest.mark.integration
def test_mongo_auto_seed_and_search():
    # Because of autouse fixture, DB is already seeded by admission_service.initialize()
    db = admission_service._db
    
    # Assert that records were inserted automatically during setup
    count = db.admission_scores.count_documents({})
    assert count > 0
    
    # Test search logic with Regex against the real MongoDB instance
    result = admission_service.search_admission_scores("Công nghệ thông tin")
    
    assert result is not None
    assert "Công nghệ thông tin" in result
    
    # Search for something that doesn't exist
    not_found = admission_service.search_admission_scores("NganhKhongBaoGioTonTai123")
    assert not_found is None

@pytest.mark.integration
def test_mongo_manual_insert_and_search():
    # Insert custom data into the testcontainer MongoDB
    db = admission_service._db
    db.admission_scores.insert_one({
        "Trường": "Test University",
        "Ngành Tiếng Việt": "Khoa học Dữ liệu",
        "Phương thức": "Xét học bạ",
        "Điểm chuẩn": "29.0",
        "Tổ hợp": "A00",
        "Ghi chú": ""
    })
    
    # Search using the service
    result = admission_service.search_admission_scores("Khoa học Dữ liệu")
    
    # Assert
    assert result is not None
    assert "Test University" in result
    assert "Khoa học Dữ liệu" in result
    assert "29.0" in result
    assert "Xét học bạ" in result
