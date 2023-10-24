package todo;

import java.time.Duration;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.RawFileBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.jmesPath;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class RecordedSimulation extends Simulation {

  private HttpProtocolBuilder httpProtocol = http
    .baseUrl("https://todosvc")
    .disableFollowRedirect()
    .disableAutoReferer()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip,deflate")
    .contentTypeHeader("application/json")
    .userAgentHeader("Apache-HttpClient/4.5.13 (Java/19.0.2)");
  
  private Map<CharSequence, String> headers_0 = Map.ofEntries(
    Map.entry("Content-Type", "application/x-www-form-urlencoded; charset=ISO-8859-1"),
    Map.entry("Proxy-Connection", "Keep-Alive")
  );
  
  private Map<CharSequence, String> headers_1 = Map.ofEntries(
    Map.entry("Proxy-Connection", "Keep-Alive"),
    Map.entry("authorization", "Bearer ${access_token}")
  );
  
  //private String uri1 = "localhost";

  private ScenarioBuilder scn = scenario("RecordedSimulation")
    .exec(
      http("request_0")
        .post("https://keycloak/realms/my-todo-app/protocol/openid-connect/token")
        .headers(headers_0)
        .formParam("grant_type", "password")
        .formParam("username", "test")
        .formParam("client_id", "my-todo-app-web-client")
        .formParam("password", "123")
        .check(status().is(200))
        .check(jsonPath("$.access_token").saveAs("access_token"))
    )
    .pause(Duration.ofMillis(678))
    .exec(
      http("request_1")
        .post("/v1/activities")
        .headers(headers_1)
        .body(RawFileBody("recordedsimulation/0001_request.json"))
        .check(status().is(200))
        .check(jsonPath("$.publicId").saveAs("publicId"))

    )
    .exec(
      http("request_2")
        .get("/v1/activities")
        .headers(headers_1)
        .check(status().is(200))
        .check(jmesPath("[?publicId=='${publicId}']"))
      );

  {
	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
  }
}
