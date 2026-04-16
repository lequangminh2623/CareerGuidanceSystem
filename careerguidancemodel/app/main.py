"""
Career Guidance System - FastAPI Backend
Main application entry point.

Bridges the Spring Boot student management system, the Next.js frontend,
and the fine-tuned Gemini model (career-guidance-v6) for career counseling
of Vietnamese high school students.
"""

import logging
import asyncio
from contextlib import asynccontextmanager

from typing import Any
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
import datetime

from app.config import settings
from app.models.schemas import HealthResponse
from app.services import gemini_service, chat_manager, admission_service
from app.routers import holland, guidance, chat

# ══════════════════════════════════════════════
#  Logging Configuration
# ══════════════════════════════════════════════

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s │ %(levelname)-7s │ %(name)s │ %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)


# ══════════════════════════════════════════════
#  Background Task: Session Cleanup
# ══════════════════════════════════════════════

async def _cleanup_loop():
    """Periodically clean up expired chat sessions every 5 minutes."""
    while True:
        await asyncio.sleep(300)  # 5 minutes
        try:
            count = chat_manager.cleanup_expired()
            if count > 0:
                logger.info("Cleaned up %d expired chat sessions", count)
        except Exception as e:
            logger.error("Error during session cleanup: %s", str(e))


# ══════════════════════════════════════════════
#  Application Lifespan
# ══════════════════════════════════════════════

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Handle startup and shutdown events."""
    # ── Startup ──
    logger.info("=" * 60)
    logger.info("  Career Guidance System - Starting up...")
    logger.info("=" * 60)

    # Initialize Gemini API
    gemini_service.initialize()
    logger.info("✓ Gemini API initialized (model: %s)", settings.GEMINI_MODEL_NAME)

    # Initialize MongoDB for admission score lookup
    admission_service.initialize()

    logger.info("✓ Spring Boot URL: %s", settings.SPRING_BOOT_BASE_URL)
    logger.info("✓ CORS origins: %s", settings.cors_origins)

    # Start background cleanup task
    cleanup_task = asyncio.create_task(_cleanup_loop())
    logger.info("✓ Session cleanup background task started")

    logger.info("=" * 60)
    logger.info("  Server ready at http://%s:%d", settings.HOST, settings.PORT)
    logger.info("  API docs at http://%s:%d/docs", settings.HOST, settings.PORT)
    logger.info("=" * 60)

    yield

    # ── Shutdown ──
    cleanup_task.cancel()
    logger.info("Career Guidance System - Shutting down...")


app = FastAPI(
    title="Career Guidance System API",
    description=(
        "API tư vấn hướng nghiệp cho học sinh THPT Việt Nam. "
        "Tích hợp mô hình Gemini đã fine-tune và hệ thống quản lý học sinh Spring Boot."
    ),
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# ══════════════════════════════════════════════
#  Global Exception Handlers
# ══════════════════════════════════════════════

def _build_exception_response(status_code: int, error_name: str, message: str, details: Any, path: str):
    return {
        "timestamp": datetime.datetime.now().isoformat(),
        "status": status_code,
        "error": error_name,
        "message": message,
        "details": details,
        "path": path
    }

@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content=_build_exception_response(
            exc.status_code,
            "HTTP Exception",
            str(exc.detail),
            None,
            request.url.path
        )
    )

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=400,
        content=_build_exception_response(
            400,
            "Validation Error",
            "Dữ liệu gửi lên không đúng định dạng",
            exc.errors(),
            request.url.path
        )
    )

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    logger.error("Unhandled exception: %s", exc)
    return JSONResponse(
        status_code=500,
        content=_build_exception_response(
            500,
            "Internal Server Error",
            "Đã có lỗi xảy ra từ máy chủ",
            None,
            request.url.path
        )
    )

# ── CORS Middleware ──
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Include Routers ──
app.include_router(holland.router)
app.include_router(guidance.router)
app.include_router(chat.router)


# ══════════════════════════════════════════════
#  Health Check
# ══════════════════════════════════════════════

@app.get(
    "/health",
    response_model=HealthResponse,
    tags=["System"],
    summary="Health check",
)
async def health_check():
    """Check if the service is alive and responding."""
    return HealthResponse(
        status="ok",
        service="career-guidance-api",
        version="1.0.0",
    )


@app.get(
    "/health/sessions",
    tags=["System"],
    summary="Active chat sessions count",
)
async def session_count():
    """Return the number of active chat sessions."""
    return {"active_sessions": chat_manager.get_active_session_count()}


# ══════════════════════════════════════════════
#  Entry Point
# ══════════════════════════════════════════════

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )
