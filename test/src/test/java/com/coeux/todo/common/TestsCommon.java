package com.coeux.todo.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestsCommon {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("realm-export.json");

    static Map<String, String> envs = new HashMap<>();
    static {
        envs.put("POSTGRES_USER","postgres");
        envs.put("POSTGRES_PASSWORD","mysecretpassword");
        envs.put("APP_DB_USER","todo_user");
        envs.put("APP_DB_PASS","mysecretpassword");
        envs.put("APP_DB_NAME","todo");
        envs.put("KC_DB_USERNAME","keycloak");
        envs.put("KC_DB_PASSWORD","keycloak123");
        envs.put("KC_DB_URL_DATABASE","keycloak");
    }
    
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:11.1")
            .withEnv(envs)
            .withFileSystemBind("C:/Users/Andres/projects/todo/todo-db/", "/docker-entrypoint-initdb.d/",
                    BindMode.READ_ONLY);

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/my-todo-app");
        registry.add("jwt.jwksURI", ()->keycloak.getAuthServerUrl() + "/realms/my-todo-app/protocol/openid-connect/certs");
    }

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @Test
    void givenAuthenticatedUser_whenGetMe_shouldReturnMyInfo() {
    }
}
