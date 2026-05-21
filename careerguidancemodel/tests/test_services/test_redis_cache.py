import pytest
import fakeredis
from app.services import redis_cache
import redis

@pytest.fixture
def mock_redis_cache(mocker):
    # Replace the Redis client with FakeRedis
    fake_redis = fakeredis.FakeRedis(decode_responses=True)
    mocker.patch("app.services.redis_cache.redis.Redis", return_value=fake_redis)
    return fake_redis

def test_initialize_success(mock_redis_cache):
    redis_cache.initialize()
    assert redis_cache.is_available() is True
    assert redis_cache._redis_client is not None

def test_initialize_failure(mocker):
    # Simulate connection error
    mocker.patch("app.services.redis_cache.redis.Redis", side_effect=redis.ConnectionError("Connection Refused"))
    redis_cache.initialize()
    assert redis_cache.is_available() is False
    assert redis_cache._redis_client is None

def test_get_and_set_cached(mock_redis_cache):
    redis_cache.initialize()
    
    prefix = "test_prefix"
    data = {"key": "value"}
    result_to_cache = "Cached Result"
    
    # Initially should be None
    assert redis_cache.get_cached(prefix, data) is None
    
    # Set cache
    redis_cache.set_cached(prefix, data, result_to_cache, ttl=60)
    
    # Now it should hit cache
    cached_result = redis_cache.get_cached(prefix, data)
    assert cached_result == result_to_cache

def test_cache_miss_with_redis_error(mocker, mock_redis_cache):
    redis_cache.initialize()
    
    # Force get to raise an error
    mocker.patch.object(redis_cache._redis_client, "get", side_effect=redis.RedisError("Some error"))
    
    result = redis_cache.get_cached("prefix", {"data": 1})
    assert result is None

def test_cache_set_with_redis_error(mocker, mock_redis_cache):
    redis_cache.initialize()
    
    # Force set to raise an error
    mocker.patch.object(redis_cache._redis_client, "setex", side_effect=redis.RedisError("Some error"))
    
    # Should not raise exception
    redis_cache.set_cached("prefix", {"data": 1}, "result")
