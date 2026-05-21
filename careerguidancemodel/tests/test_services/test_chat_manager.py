import pytest
from app.services import chat_manager
import time

def test_create_session():
    # Arrange & Act
    initial_context = {"key": "value"}
    session_id = chat_manager.create_session(initial_context)

    # Assert
    assert session_id is not None
    assert type(session_id) is str
    
    session = chat_manager.get_session(session_id)
    assert session is not None
    assert session.initial_context == initial_context
    assert session.messages == []
    assert hasattr(session, "last_active")

def test_add_message():
    # Arrange
    session_id = chat_manager.create_session()

    # Act
    chat_manager.add_message(session_id, "user", "Hello")
    chat_manager.add_message(session_id, "model", "Hi there")

    # Assert
    history = chat_manager.get_history(session_id)
    assert len(history) == 2
    assert history[0]["role"] == "user"
    assert history[0]["content"] == "Hello"
    assert history[1]["role"] == "model"
    assert history[1]["content"] == "Hi there"

def test_get_session_not_found():
    # Act
    session = chat_manager.get_session("invalid_session_id")
    
    # Assert
    assert session is None

def test_get_history_not_found():
    # Act
    history = chat_manager.get_history("invalid_session_id")
    
    # Assert
    assert history is None

def test_get_initial_context():
    # Arrange
    session_id = chat_manager.create_session({"context": "data"})
    
    # Act
    context = chat_manager.get_initial_context(session_id)
    
    # Assert
    assert context == {"context": "data"}

def test_cleanup_expired(mocker):
    # Arrange
    session_id_1 = chat_manager.create_session()
    session_id_2 = chat_manager.create_session()
    
    # Mock time.time to simulate expired session 1
    original_time = time.time
    
    # session_id_1 is expired, session_id_2 is not.
    # The default timeout is 3600s. We'll set session 1's last_active to 4000s ago.
    chat_manager._sessions[session_id_1].last_active = original_time() - 4000
    
    # Act
    cleaned_count = chat_manager.cleanup_expired()
    
    # Assert
    assert cleaned_count == 1
    assert chat_manager.get_session(session_id_1) is None
    assert chat_manager.get_session(session_id_2) is not None

def test_get_active_session_count():
    # Clean all sessions first
    chat_manager._sessions = {}
    
    # Arrange
    chat_manager.create_session()
    chat_manager.create_session()
    
    # Act
    count = chat_manager.get_active_session_count()
    
    # Assert
    assert count == 2
