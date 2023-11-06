package com.coeux.todo;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

@Testcontainers
@SpringBootTest(
    classes = TodoApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class TestActivitiesService {

    @LocalServerPort
	private int port;
    
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
    .withDatabaseName("todo")
    .withClasspathResourceMapping("todo-db", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer()
        .withEnv("DB_VENDOR", "h2")
        .withRealmImportFile("realm-export.json");

    @DynamicPropertySource
    private static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("jwt.jwksURI", ()->keycloak.getAuthServerUrl() + "/realms/my-todo-app/protocol/openid-connect/certs");
    }

    @Test
    void givenAuthenticatedUser_whenGetActivities_shouldReturnActivities() {
        var serviceURI = URI.create("http://localhost:"+port+"/v1/activities");
        var keyCloakURI = URI.create(keycloak.getAuthServerUrl() + "/realms/my-todo-app/protocol/openid-connect/token");
        var tok = assertToken(keyCloakURI);

        assertPostActivity(tok, serviceURI);
        assetGetActivities(tok, serviceURI);
    }

    public String assertToken(URI keyCloakURI) {

        var formData = Map.of(
                "grant_type", "password",
                "client_id", "my-todo-app-web-client",
                "username", "test",
                "password", "123");

        var token = given()
                .formParams(formData)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .expect()
                .statusCode(200)
                .body(containsString("access_token"))
                .when()
                .post(keyCloakURI)
                .jsonPath()
                .get("access_token");

        return "Bearer " + token;
    }

    public void assertPostActivity(String tok, URI serviceURI) {
        given()
                .body(BODY_ACT)
                .header("content-type", "application/json")
                .header("Authorization", tok)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .post(serviceURI);
    }

    public void assetGetActivities(String tok, URI serviceURI) {
        given()
                .header("content-type", "application/json")
                .header("Authorization", tok)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .get(serviceURI);
    }

    private static String BODY_ACT = """
            {
                "name":"Link-2",
                "description":"This is a link",
                "type":"LINK",
                "state":0,
                "status":0,
                "tags":["t1","t2"],
                "extraData": {
                    "uno":"1",
                    "dos":2
                },
                "media":[
                    {
                        "name":"media1",
                        "description":"media1",
                        "type":"IMAGE",
                        "uri":"http://www.a.com/j.jpg",
                        "extraData": {
                            "uno":"1",
                            "dos":2
                        }
                    }
                ]
            }

                """;


}
