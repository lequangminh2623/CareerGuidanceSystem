package com.lqm.academic_service.configs;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(24))
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new org.springframework.data.redis.serializer.JdkSerializationRedisSerializer()))
                                .disableCachingNullValues();

                // Per-cache TTL configurations
                Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                                "academic::subjects", defaultConfig.entryTtl(Duration.ofHours(24)),
                                "academic::grades", defaultConfig.entryTtl(Duration.ofHours(24)),
                                "academic::semesters", defaultConfig.entryTtl(Duration.ofHours(24)),
                                "academic::years", defaultConfig.entryTtl(Duration.ofHours(24)),
                                "academic::curriculums", defaultConfig.entryTtl(Duration.ofHours(12)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}
