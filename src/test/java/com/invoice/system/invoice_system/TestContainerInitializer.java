package com.invoice.system.invoice_system;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainerInitializer {
    public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:alpine");

    @DynamicPropertySource
    static void setConfigurationProperties(DynamicPropertyRegistry registry) {
        registry.add("db.url", container::getJdbcUrl);
        registry.add("db.username", container::getUsername);
        registry.add("db.password", container::getPassword);
    }

    @BeforeAll
    public static void beforeAll() {
        container.start();
    }

    @AfterAll
    public static void afterAll() {
        container.stop();
    }
}