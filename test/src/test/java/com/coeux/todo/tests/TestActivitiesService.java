package com.coeux.todo.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import java.io.File;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.restassured.http.ContentType;

@Testcontainers
public class TestActivitiesService {

    @Container
    public static DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("../compose.yaml")).withLocalCompose(true)
                    .withExposedService("keycloak", 8080).withExposedService("todosvc", 8080);

    static URI KeyCloakURI = null;
    static URI ServiceURI = null;

    @BeforeAll
    public static void setProperties() {
        String url = environment.getServiceHost("keycloak", 8080) +
                ":"
                + environment.getServicePort("keycloak", 8080);
        KeyCloakURI =
                URI.create("http://" + url + "/realms/my-todo-app/protocol/openid-connect/token");

        String urlsvc = environment.getServiceHost("todosvc", 8080) + ":"
                + environment.getServicePort("todosvc", 8080);

        ServiceURI = URI.create("http://" + urlsvc + "/v1/activities");
    }

    @Test
    void givenAuthenticatedUser_whenGetActivities_shouldReturnActivities() {
        var tok = assertToken();
        assertPostActivity(tok);
        assetGetActivities(tok);
    }

    public String assertToken() {

        var formData = Map.of(
                "grant_type", "password",
                "client_id", "my-todo-app-web-client",
                "username", "test",
                "password", "123");

        var token = given()
                //.proxy("127.0.0.1", 8000)
                .formParams(formData)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .expect()
                .statusCode(200)
                .body(containsString("access_token"))
                .when()
                .post(KeyCloakURI)
                .jsonPath()
                .get("access_token");

        return "Bearer " + token;
    }

    public void assertPostActivity(String tok) {
        given()
                //.proxy("127.0.0.1", 8000)
                .body(BODY_ACT)
                .header("content-type", "application/json")
                .header("Authorization", tok)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .post(ServiceURI);
    }

    public void assetGetActivities(String tok) {
        given()
                //.proxy("127.0.0.1", 8000)
                .header("content-type", "application/json")
                .header("Authorization", tok)
                .expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .when()
                .get(ServiceURI);
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
