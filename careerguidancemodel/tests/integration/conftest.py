import pytest
import os
from unittest.mock import patch, PropertyMock
from pymongo import MongoClient
from testcontainers.mongodb import MongoDbContainer
from testcontainers.redis import RedisContainer
from app.config import settings
from app.services import redis_cache, admission_service

@pytest.fixture(scope="session")
def mongodb_container():
    """Starts a MongoDB Testcontainer."""
    with MongoDbContainer("mongo:6.0") as mongo:
        yield mongo

@pytest.fixture(scope="session")
def redis_container():
    """Starts a Redis Testcontainer."""
    with RedisContainer("redis:7.0-alpine") as redis_server:
        yield redis_server

@pytest.fixture(autouse=True)
def setup_services(mongodb_container, redis_container):
    """Automatically initialize services using the testcontainers before each test."""
    
    # 1. Patch MONGO_URI
    with patch('app.config.Settings.MONGO_URI', new_callable=PropertyMock) as mock_uri:
        mock_uri.return_value = mongodb_container.get_connection_url()
        
        # Override DB Name
        settings.MONGO_DB = "test_career_guidance"
        
        # 2. Override REDIS settings
        settings.REDIS_HOST = redis_container.get_container_host_ip()
        settings.REDIS_PORT = int(redis_container.get_exposed_port(6379))
        settings.REDIS_PASSWORD = ""
        
        # Reset services
        admission_service._client = None
        admission_service._db = None
        redis_cache._redis_client = None
        
        # Clean Redis
        redis_cache.initialize()
        redis_cache._redis_client.flushdb()
        
        # Clean Mongo
        client = MongoClient(mongodb_container.get_connection_url())
        client.drop_database("test_career_guidance")
        
        # Initialize (will auto-seed)
        admission_service.initialize()
        
        yield
