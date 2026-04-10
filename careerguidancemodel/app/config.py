"""
Application configuration using pydantic-settings.
Reads environment variables from .env file.
"""

from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # ── Gemini API ──
    GEMINI_API_KEY: str = "your-gemini-api-key-here"
    GEMINI_MODEL_NAME: str = "tunedModels/career-guidance-v6"

    # ── Spring Boot API Gateway ──
    SPRING_BOOT_BASE_URL: str = "http://localhost:8080"

    # ── CORS ──
    ALLOWED_ORIGINS: str = "http://localhost:3000"

    # ── Server ──
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    @property
    def cors_origins(self) -> List[str]:
        """Parse comma-separated ALLOWED_ORIGINS into a list."""
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",")]

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# Singleton instance
settings = Settings()
