package com.lqm.score_service;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

        @SuppressWarnings("resource")
        static final PostgreSQLContainer postgresContainer = new PostgreSQLContainer(
                        DockerImageName.parse("postgres:16-alpine"))
                        .withDatabaseName("score_test_db")
                        .withUsername("test_user")
                        .withPassword("test_pass")
                        .withReuse(true);

        @SuppressWarnings("resource")
        static final GenericContainer<?> rabbitMQContainer = new GenericContainer<>(
                        DockerImageName.parse("rabbitmq:3.13-alpine"))
                        .withExposedPorts(5672)
                        .withEnv("RABBITMQ_DEFAULT_USER", "guest")
                        .withEnv("RABBITMQ_DEFAULT_PASS", "guest")
                        .withReuse(true);

        @SuppressWarnings("resource")
        static final GenericContainer<?> redisContainer = new GenericContainer<>(
                        DockerImageName.parse("redis:7.2-alpine"))
                        .withExposedPorts(6379)
                        .withReuse(true);

        static {
                postgresContainer.start();
                rabbitMQContainer.start();
                redisContainer.start();
        }

        @DynamicPropertySource
        static void registerDynamicProperties(DynamicPropertyRegistry registry) {

                // --- PostgreSQL ---
                registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
                registry.add("spring.datasource.username", postgresContainer::getUsername);
                registry.add("spring.datasource.password", postgresContainer::getPassword);
                registry.add("spring.datasource.hikari.maximum-pool-size", () -> 2);

                // --- RabbitMQ ---
                registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
                registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getMappedPort(5672));
                registry.add("spring.rabbitmq.username", () -> "guest");
                registry.add("spring.rabbitmq.password", () -> "guest");

                // --- Redis ---
                registry.add("spring.data.redis.host", redisContainer::getHost);
                registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
                registry.add("spring.data.redis.password", () -> "");
        }
}
