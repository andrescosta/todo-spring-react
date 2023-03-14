package com.coeux.todo.common;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.json.JacksonJsonParser;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestCompose {

        @Container
        public static DockerComposeContainer<?> environment = new DockerComposeContainer<>(
                        new File("C:/Users/Andres/projects/todo/compose.yaml")).withLocalCompose(true)
                        .withExposedService("keycloak", 8080)
                        .withExposedService("todosvc", 8080);

        @Test
        void givenAuthenticatedUser_whenGetMe_shouldReturnMyInfo() throws Exception {
                try {
                        var tok = getToken();

                        System.out.println(getActivities(tok));
                } catch (Exception e) {
                        throw (e);
                }
        }

        public String getToken() throws IOException, InterruptedException {
                String url = environment.getServiceHost("keycloak", 8080)
                                + ":" +
                                environment.getServicePort("keycloak", 8080);

                var formData = Map.of(
                        "grant_type", "password",
                        "client_id", "my-todo-app-web-client",
                        "username", "test",
                        "password", "123");

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + url
                                                + "/realms/my-todo-app/protocol/openid-connect/token"))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(HttpRequest.BodyPublishers.ofString(get(formData)))
                                .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                JacksonJsonParser jsonParser = new JacksonJsonParser();
                var tok = "Bearer " + jsonParser.parseMap(body)
                                .get("access_token")
                                .toString();

                return tok;
        }

        public String getActivities(String tok) throws IOException, InterruptedException {
                String urlsvc = environment.getServiceHost("todosvc", 8080)
                                + ":" +
                                environment.getServicePort("todosvc", 8080);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest requestget = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + urlsvc + "/activities"))
                                .header("Content-Type", "application/json")
                                .header("Authorization", tok)
                                .GET().build();
                HttpResponse<String> responseget = client.send(requestget,
                                HttpResponse.BodyHandlers.ofString());
                var bodyget = responseget.body();
                return bodyget;

        }

        private String get(Map<String, String> map){
                return map
                    .entrySet()
                    .stream()
                    .map(entry -> Stream.of(
                            URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                            .collect(Collectors.joining("="))
                    ).collect(Collectors.joining("&"));
        }
        private static String getFormDataAsString(Map<String, String> formData) {
                StringBuilder formBodyBuilder = new StringBuilder();
                for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
                        if (formBodyBuilder.length() > 0) {
                                formBodyBuilder.append("&");
                        }
                        formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
                        formBodyBuilder.append("=");
                        formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
                }
                return formBodyBuilder.toString();
        }
}
