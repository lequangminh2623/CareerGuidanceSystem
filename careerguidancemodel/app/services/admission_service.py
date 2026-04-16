"""
Admission score lookup service.
Queries MongoDB for admission cutoff scores (điểm chuẩn) by major name.
"""

import os
import logging
import re
import pandas as pd
from typing import Optional, List, Dict, Any
from pymongo import MongoClient

from app.config import settings

logger = logging.getLogger(__name__)

_client: Optional[MongoClient] = None
_db = None


def initialize():
    """Initialize MongoDB connection. Call on app startup."""
    global _client, _db
    _client = MongoClient(settings.MONGO_URI)
    _db = _client[settings.MONGO_DB]
    
    # Verify connection
    try:
        _client.admin.command("ping")
        count = _db.admission_scores.count_documents({})
        if count == 0:
            logger.info("! Database rỗng. Đang tự động đổ dữ liệu từ file CSV...")
            auto_seed()
        else:
            logger.info("✓ MongoDB connected (admission_scores: %d records)", count)
    except Exception as e:
        logger.warning("MongoDB connection issue: %s", str(e))


def auto_seed():
    """Hàm tự động seed dữ liệu từ file csv nằm trong root service"""
    csv_path = "diem_chuan.csv"  # File này đã được COPY vào /app trong Docker
    if os.path.exists(csv_path):
        try:
            df = pd.read_csv(csv_path)
            df = df.fillna("")
            records = df.to_dict(orient="records")
            if records:
                _db.admission_scores.insert_many(records)
                logger.info("✓ Tự động Seed thành công %d bản ghi.", len(records))
        except Exception as e:
            logger.error("✘ Lỗi khi tự động seed: %s", str(e))
    else:
        logger.warning("! Không tìm thấy file diem_chuan.csv để tự động seed")


def search_admission_scores(query: str) -> Optional[str]:
    """
    Search for admission scores matching the given major/field name.
    
    Args:
        query: The major name to search for (e.g., "Công nghệ thông tin")
    
    Returns:
        A formatted markdown string with results, or None if no results found.
    """
    if _db is None:
        logger.warning("MongoDB not initialized")
        return None

    # Clean the query
    query = query.strip()
    if not query:
        return None

    # Search with case-insensitive regex on the field "Ngành Tiếng Việt"
    pattern = re.compile(re.escape(query), re.IGNORECASE)
    results: List[Dict[str, Any]] = list(
        _db.admission_scores.find(
            {"Ngành Tiếng Việt": pattern},
            {"_id": 0}  # Exclude MongoDB _id
        ).limit(30)  # Limit to avoid huge responses
    )

    if not results:
        return None

    # Format into readable markdown
    return _format_results(query, results)


def _format_results(query: str, results: List[Dict[str, Any]]) -> str:
    """Format MongoDB results into a chat-friendly markdown response."""
    
    # Group by school
    schools: Dict[str, List[Dict[str, Any]]] = {}
    for r in results:
        school = r.get("Trường", "Không rõ")
        # Clean school name: remove "Điểm chuẩn năm 2024 - XXX - " prefix
        school_clean = school
        parts = school.split(" - ", 2)
        if len(parts) >= 3:
            school_clean = parts[2].strip()
        elif len(parts) == 2:
            school_clean = parts[1].strip()
        
        if school_clean not in schools:
            schools[school_clean] = []
        schools[school_clean].append(r)

    major_name = results[0].get("Ngành Tiếng Việt", query)
    
    lines = [f"📊 **Điểm chuẩn ngành {major_name}** (năm 2024)\n"]
    
    for school_name, entries in schools.items():
        lines.append(f"🏫 **{school_name}**")
        for entry in entries:
            method = entry.get("Phương thức", "")
            score = entry.get("Điểm chuẩn", "")
            combo = entry.get("Tổ hợp", "")
            note = entry.get("Ghi chú", "")
            
            # Build info line
            detail = f"  - {method}: **{score}**"
            if combo:
                detail += f" ({combo})"
            if note:
                detail += f" _{note}_"
            lines.append(detail)
        lines.append("")  # blank line between schools

    lines.append(f"_Hiển thị {len(results)} kết quả. Dữ liệu tham khảo năm 2024._")
    
    return "\n".join(lines)


def extract_major_from_message(message: str) -> Optional[str]:
    """
    Try to extract the major/field name from a user's chat message
    about admission scores.
    
    Args:
        message: The user's message (e.g., "Điểm chuẩn ngành Công nghệ thông tin?")
    
    Returns:
        The extracted major name, or None if not detectable.
    """
    msg = message.strip()
    
    # Patterns to match "điểm chuẩn ngành X" or "điểm chuẩn X"
    patterns = [
        r"(?:điểm\s*chuẩn|diem\s*chuan)\s+(?:ngành|nganh)?\s*(.+?)(?:\?|$|\.)",
        r"(?:điểm\s*chuẩn|diem\s*chuan)\s+(.+?)(?:\?|$|\.)",
    ]
    
    for pattern in patterns:
        match = re.search(pattern, msg, re.IGNORECASE)
        if match:
            major = match.group(1).strip().rstrip("?.! ")
            if len(major) >= 2:
                return major

    return None
