import pytest
import time
from app.services import redis_cache

@pytest.mark.integration
def test_redis_set_and_get():
    # Because of the autouse fixture in conftest.py, 
    # redis_cache is already connected to the Testcontainer Redis instance
    # and the DB is flushed before this test starts.
    
    assert redis_cache.is_available() is True
    
    prefix = "integration_test"
    data = {"user_id": 123, "action": "test"}
    result = "This is a real cached result"
    
    # Check cache miss
    cached = redis_cache.get_cached(prefix, data)
    assert cached is None
    
    # Set cache
    redis_cache.set_cached(prefix, data, result, ttl=60)
    
    # Check cache hit
    cached_hit = redis_cache.get_cached(prefix, data)
    assert cached_hit == result

@pytest.mark.integration
def test_redis_ttl_expiration():
    prefix = "ttl_test"
    data = {"temp": True}
    
    # Set cache with 1 second TTL
    redis_cache.set_cached(prefix, data, "temp_data", ttl=1)
    
    # Immediately accessible
    assert redis_cache.get_cached(prefix, data) == "temp_data"
    
    # Wait for expiration
    time.sleep(1.5)
    
    # Should be gone
    assert redis_cache.get_cached(prefix, data) is None
