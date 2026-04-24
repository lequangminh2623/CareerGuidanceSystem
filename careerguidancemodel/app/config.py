"""
Application configuration using pydantic-settings.
Reads environment variables from .env file.
"""

from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):

    # ── Spring Boot API Gateway ──
    SPRING_BOOT_BASE_URL: str = "http://localhost:8080"

    # ── MongoDB ──
    MONGO_ROOT_USER: str = "mongodb"
    MONGO_ROOT_PASSWORD: str = "mongodb"
    MONGO_HOST: str = "mongodb"
    MONGO_PORT: int = 27017
    MONGO_DB: str = "career_guidance"

    @property
    def MONGO_URI(self) -> str:
        return f"mongodb://{self.MONGO_ROOT_USER}:{self.MONGO_ROOT_PASSWORD}@{self.MONGO_HOST}:{self.MONGO_PORT}/?authSource=admin"

    # ── CORS ──
    ALLOWED_ORIGINS: str = "http://localhost:3000"

    # ── Server ──
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    # ── Gemini API (AI Studio & Vertex) ──
    GEMINI_API_KEY: str = ""
    GEMINI_MODEL_NAME: str = "career-guidance-v6"
    GOOGLE_CLOUD_PROJECT: str = ""
    GOOGLE_CLOUD_REGION: str = "us-central1"

    # ── Redis Cache ──
    REDIS_HOST: str = "redis"
    REDIS_PORT: int = 6379
    REDIS_PASSWORD: str = ""
    REDIS_CACHE_TTL: int = 7200  # 2 hours in seconds

    @property
    def cors_origins(self) -> List[str]:
        """Parse comma-separated ALLOWED_ORIGINS into a list."""
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",")]

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# Singleton instance
settings = Settings()
