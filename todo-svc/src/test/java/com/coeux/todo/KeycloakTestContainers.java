package com.coeux.todo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

import dasniko.testcontainers.keycloak.KeycloakContainer;

//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class KeycloakTestContainers {

    private static final Logger log = LoggerFactory.getLogger(KeycloakTestContainers.class);
    
    private static KeycloakContainer keycloak;
    private static PostgreSQLContainer<?> postgreSQLContainer;

    static {
        keycloak = new KeycloakContainer().withRealmImportFile("realm-export.json");
        keycloak.start();

        postgreSQLContainer = new PostgreSQLContainer<>("postgres:11.1")
                .withDatabaseName("integration-tests-db")
                .withUsername("sa")
                .withPassword("sa");

        if (null == postgreSQLContainer) {
            postgreSQLContainer = new PostgreSQLContainer<>("postgres:14.5")
                    .withDatabaseName("jhipsterSampleApplication")
                    .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                    .withLogConsumer(new Slf4jLogConsumer(log))
                    .withReuse(true);

        }
        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
    }

    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/my-todo-app");
    }

    public static String getJaneDoeBearer() throws URISyntaxException {
        URI authorizationURI = new URIBuilder(
                keycloak.getAuthServerUrl() + "/realms/baeldung/protocol/openid-connect/token").build();
        WebClient webclient = WebClient.builder().build();
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", Collections.singletonList("password"));
        formData.put("client_id", Collections.singletonList("baeldung-api"));
        formData.put("username", Collections.singletonList("jane.doe@baeldung.com"));
        formData.put("password", Collections.singletonList("s3cr3t"));

        String result = webclient.post()
                .uri(authorizationURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return "Bearer " + jsonParser.parseMap(result)
                .get("access_token")
                .toString();
    }

    @Test
    void givenAuthenticatedUser_whenGetMe_shouldReturnMyInfo() {
        try {
            given().header("Authorization", getJaneDoeBearer())
                    .when()
                    .get("/users/me")
                    .then()
                    .body("username", equalTo("janedoe"))
                    .body("lastname", equalTo("Doe"))
                    .body("firstname", equalTo("Jane"))
                    .body("email", equalTo("jane.doe@baeldung.com"));
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}