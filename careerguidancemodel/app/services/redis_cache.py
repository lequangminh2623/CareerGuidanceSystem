"""
Redis cache helper for caching AI guidance results.
Uses JSON serialization and hash-based keys.
"""

import hashlib
import json
import logging
from typing import Optional, Any

import redis

from app.config import settings

logger = logging.getLogger(__name__)

# Redis client singleton
_redis_client: Optional[redis.Redis] = None


def initialize():
    """Initialize Redis connection. Called during app startup."""
    global _redis_client
    try:
        _redis_client = redis.Redis(
            host=settings.REDIS_HOST,
            port=settings.REDIS_PORT,
            password=settings.REDIS_PASSWORD if settings.REDIS_PASSWORD else None,
            db=1,  # Use DB 1 to separate from rate limiter (DB 0)
            decode_responses=True,
            socket_connect_timeout=5,
            retry_on_timeout=True,
        )
        # Test connection
        _redis_client.ping()
        logger.info("✓ Redis cache connected (%s:%d, db=1)", settings.REDIS_HOST, settings.REDIS_PORT)
    except redis.ConnectionError as e:
        logger.warning("⚠ Redis cache unavailable: %s. Caching disabled.", str(e))
        _redis_client = None


def _make_key(prefix: str, data: Any) -> str:
    """Create a deterministic cache key by hashing the input data."""
    serialized = json.dumps(data, sort_keys=True, default=str)
    data_hash = hashlib.sha256(serialized.encode()).hexdigest()[:16]
    return f"guidance::{prefix}:{data_hash}"


def get_cached(prefix: str, data: Any) -> Optional[str]:
    """
    Try to get a cached result.
    Returns the cached JSON string, or None if cache miss.
    """
    if _redis_client is None:
        return None

    key = _make_key(prefix, data)
    try:
        cached = _redis_client.get(key)
        if cached:
            logger.debug("Cache HIT: %s", key)
            return cached
        logger.debug("Cache MISS: %s", key)
        return None
    except redis.RedisError as e:
        logger.warning("Redis get error: %s", str(e))
        return None


def set_cached(prefix: str, data: Any, result: str, ttl: Optional[int] = None):
    """
    Store a result in cache.
    Args:
        prefix: Cache key prefix (e.g., 'holland', 'academic', 'full')
        data: Input data used to generate cache key
        result: The result string to cache
        ttl: Time to live in seconds (default from settings)
    """
    if _redis_client is None:
        return

    key = _make_key(prefix, data)
    if ttl is None:
        ttl = settings.REDIS_CACHE_TTL

    try:
        _redis_client.setex(key, ttl, result)
        logger.debug("Cache SET: %s (TTL=%ds)", key, ttl)
    except redis.RedisError as e:
        logger.warning("Redis set error: %s", str(e))


def is_available() -> bool:
    """Check if Redis cache is available."""
    return _redis_client is not None
