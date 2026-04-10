"""
In-memory chat session manager.
Stores conversation history and initial guidance context per session.
Auto-cleans expired sessions.
"""

import logging
import uuid
import time
from typing import Dict, List, Optional
from dataclasses import dataclass, field

logger = logging.getLogger(__name__)

# Session expiry: 1 hour
SESSION_TTL_SECONDS = 3600


@dataclass
class ChatSession:
    """Represents a single chat session."""

    session_id: str
    created_at: float
    last_active: float
    initial_context: Dict[str, str] = field(default_factory=dict)
    messages: List[Dict[str, str]] = field(default_factory=list)


# In-memory store
_sessions: Dict[str, ChatSession] = {}


def create_session(initial_context: Optional[Dict[str, str]] = None) -> str:
    """
    Create a new chat session.

    Args:
        initial_context: Dict with optional keys "holland_result"
                        and/or "academic_result"

    Returns:
        The new session ID (UUID string)
    """
    session_id = str(uuid.uuid4())
    now = time.time()

    _sessions[session_id] = ChatSession(
        session_id=session_id,
        created_at=now,
        last_active=now,
        initial_context=initial_context or {},
        messages=[],
    )

    logger.info("Created chat session: %s", session_id)
    return session_id


def get_session(session_id: str) -> Optional[ChatSession]:
    """
    Retrieve a session by ID. Returns None if not found or expired.
    """
    session = _sessions.get(session_id)
    if session is None:
        return None

    # Check if expired
    if time.time() - session.last_active > SESSION_TTL_SECONDS:
        logger.info("Session expired: %s", session_id)
        del _sessions[session_id]
        return None

    return session


def add_message(session_id: str, role: str, content: str) -> bool:
    """
    Append a message to the session's conversation history.

    Args:
        session_id: The session ID
        role: "user" or "model"
        content: Message text

    Returns:
        True if successful, False if session not found
    """
    session = get_session(session_id)
    if session is None:
        return False

    session.messages.append({"role": role, "content": content})
    session.last_active = time.time()
    return True


def get_history(session_id: str) -> Optional[List[Dict[str, str]]]:
    """
    Get the full conversation history for a session.

    Returns:
        List of {"role": str, "content": str} or None if session not found
    """
    session = get_session(session_id)
    if session is None:
        return None
    return session.messages


def get_initial_context(session_id: str) -> Optional[Dict[str, str]]:
    """
    Get the initial guidance context for a session.

    Returns:
        Dict with "holland_result" and/or "academic_result",
        or None if session not found
    """
    session = get_session(session_id)
    if session is None:
        return None
    return session.initial_context


def cleanup_expired() -> int:
    """
    Remove all expired sessions from memory.

    Returns:
        Number of sessions cleaned up
    """
    now = time.time()
    expired_ids = [
        sid
        for sid, session in _sessions.items()
        if now - session.last_active > SESSION_TTL_SECONDS
    ]

    for sid in expired_ids:
        del _sessions[sid]

    if expired_ids:
        logger.info("Cleaned up %d expired sessions", len(expired_ids))

    return len(expired_ids)


def get_active_session_count() -> int:
    """Return the number of active (non-expired) sessions."""
    return len(_sessions)
