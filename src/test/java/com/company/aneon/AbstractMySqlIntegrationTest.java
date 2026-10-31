package com.company.aneon;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.MySQLContainer;

/**
 * Nen test tich hop MySQL that theo rule 07: khong dung H2/mock de ket luan
 * transaction/constraint/khoa hang.
 *
 * Hai che do:
 * - Mac dinh: Testcontainers MySQL 8.0 (can Docker).
 * - Docker khong kha dung: dat bien moi truong ANEON_IT_MYSQL_URL (va
 *   ANEON_IT_MYSQL_USER/ANEON_IT_MYSQL_PASSWORD) tro toi mot MySQL 8.0 that
 *   (VD chay trong WSL). Van la MySQL cung major, khong phai H2/mock.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractMySqlIntegrationTest {

    private static final String EXTERNAL_URL = System.getenv("ANEON_IT_MYSQL_URL");

    private static final MySQLContainer<?> MYSQL = EXTERNAL_URL == null
            ? new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("qldapm_aneon")
                    .withUsername("aneon")
                    .withPassword("aneon")
            : null;

    static {
        if (MYSQL != null) {
            MYSQL.start();
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        if (EXTERNAL_URL != null) {
            var env = System.getenv();
            registry.add("spring.datasource.url", () -> EXTERNAL_URL);
            registry.add("spring.datasource.username",
                    () -> env.getOrDefault("ANEON_IT_MYSQL_USER", "aneon"));
            registry.add("spring.datasource.password",
                    () -> env.getOrDefault("ANEON_IT_MYSQL_PASSWORD", "aneon"));
        } else {
            registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
            registry.add("spring.datasource.username", MYSQL::getUsername);
            registry.add("spring.datasource.password", MYSQL::getPassword);
        }
    }
}
