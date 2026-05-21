import pytest
from app.services import gemini_service
from app.models.schemas import HollandResult, HollandCode, StudentScores, StudentProfile, StudentSurvey

@pytest.fixture
def mock_vertexai_model(mocker):
    # Mock VertexGenerativeModel
    mock_model_class = mocker.patch("app.services.gemini_service.VertexGenerativeModel")
    mock_model_instance = mock_model_class.return_value
    
    # Mock generate_content response
    mock_response = mocker.Mock()
    mock_response.text = "Mocked AI Response"
    mock_model_instance.generate_content.return_value = mock_response
    
    # Mock chat session
    mock_chat = mocker.Mock()
    mock_chat_response = mocker.Mock()
    mock_chat_response.text = "Mocked Chat Reply"
    mock_chat.send_message.return_value = mock_chat_response
    mock_model_instance.start_chat.return_value = mock_chat
    
    return mock_model_instance

def test_initialize(mocker):
    mocker.patch("app.services.gemini_service.vertexai.init")
    mocker.patch("app.services.gemini_service.VertexGenerativeModel")
    
    gemini_service.initialize()
    
    assert gemini_service._finetuned_model is not None
    assert gemini_service._chat_model is not None

def test_get_holland_guidance_success(mocker, mock_vertexai_model):
    gemini_service._finetuned_model = mock_vertexai_model
    
    holland = HollandResult(code1=HollandCode.R, code2=HollandCode.I, code3=HollandCode.A)
    result = gemini_service.get_holland_guidance(holland)
    
    assert result == "Mocked AI Response"
    mock_vertexai_model.generate_content.assert_called_once()

def test_get_holland_guidance_error(mocker, mock_vertexai_model):
    gemini_service._finetuned_model = mock_vertexai_model
    mock_vertexai_model.generate_content.side_effect = Exception("API Error")
    
    holland = HollandResult(code1=HollandCode.R, code2=HollandCode.I, code3=HollandCode.A)
    
    with pytest.raises(gemini_service.GeminiApiError):
        gemini_service.get_holland_guidance(holland)

def test_get_academic_guidance_success(mocker, mock_vertexai_model):
    gemini_service._finetuned_model = mock_vertexai_model
    
    scores = StudentScores(
        math_score=8.0, history_score=7.0, physics_score=8.0,
        chemistry_score=8.0, biology_score=7.0, english_score=8.0, geography_score=7.0
    )
    profile = StudentProfile(gender="nam", absences=0)
    survey = StudentSurvey(has_part_time_job=False, extracurricular_activities=True, self_study_hours=10)
    
    result = gemini_service.get_academic_guidance(scores, profile, survey)
    
    assert result == "Mocked AI Response"
    mock_vertexai_model.generate_content.assert_called_once()

def test_chat_followup_with_initial_context(mocker, mock_vertexai_model):
    gemini_service._chat_model = mock_vertexai_model
    
    initial_context = {
        "holland_result": "You are a Realist.",
        "academic_result": "You should be an Engineer."
    }
    history = [{"role": "user", "content": "What else?"}]
    
    result = gemini_service.chat_followup("More detail please", history, initial_context)
    
    assert result == "Mocked Chat Reply"
    mock_vertexai_model.start_chat.assert_called_once()
    mock_vertexai_model.start_chat.return_value.send_message.assert_called_with("More detail please")

def test_not_initialized_error():
    gemini_service._finetuned_model = None
    gemini_service._chat_model = None
    
    holland = HollandResult(code1=HollandCode.R, code2=HollandCode.I, code3=HollandCode.A)
    
    with pytest.raises(gemini_service.GeminiApiError, match="Gemini chưa được khởi tạo"):
        gemini_service.get_holland_guidance(holland)
