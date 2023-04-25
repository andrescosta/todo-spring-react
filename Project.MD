# Project TODO
## Implementation Platform
* Java 19
* Spring Boot

### Helpers
* Error handling
https://github.com/wimdeblauwe/error-handling-spring-boot-starter

### Testing
```mermaid
graph TD;
    A-->B;
    A-->C;
    B-->D;
    C-->D;
```
- With Test containers 
- Performance 
- UI Automation (not implemented) 
  - https://tomaszs2.medium.com/jest-29-5-0-will-run-your-tests-randomly-ec39a27cf09c 

## Observability
> (metrics, logs, traces, continuous profiling)
- Prometheus
- Grafana 
- Loki  
- Distributed tracing 
- Profiling


* Documentation:
- https://springdoc.org/v2/ 
- https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/
https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestControllerAdvice.html


### References
https://12factor.net/
https://www.youtube.com/watch?v=7swoLEqABhQ
https://zipkin.io/
  - https://k6.io/
  - https://gatling.io/



* Nginx

Future work


CI/CD
- Explore github actions, etc.
- Production configuration
Easily generate Terraform and Bicep templates for your project.
Automate your infrastructure using GitOps best practices with GitHub Actions.
https://nubesgen.com/

KUBERNETES
- Service mesh ISTIO
- Kafka AND WEBSOCKETS
- CLOUD PROVIDERS



https://github.com/TNG/ArchUnit

SERVICE:
GRAIN https://grain-lang.org/
ROC https://www.roc-lang.org/
Elixir: https://twitter.com/elixirphoenix
Scala: 
https://tyrian.indigoengine.io/, 
https://www.youtube.com/watch?v=gIcHeE5z7Oc
https://rockthejvm.com/p/typelevel-rite-of-passage
https://github.com/temporalio/temporal
https://twitter.com/maxandersen/status/1642171871268306945

Typescript
Rust
Kotlin
Clojure
Golang
Quarkus
https://twitter.com/micronautfw
https://helidon.io/nima
vertx
https://start.microprofile.io/
C#.NET https://github.com/louthy/language-ext https://github.com/la-yumba/functional-csharp-code https://github.com/la-yumba/functional-csharp-code-2
F#.NET https://fsharpforfunandprofit.com/
Python


WEB:
https://developer.microsoft.com/en-us/fluentui#/
https://tailwindcss.com/
https://daisyui.com/
https://nuxt.com/
https://svelte.dev/
Boot + Thymeleaf + HTMX
https://github.com/TanStack/query
https://kentcdodds.com/blog/application-state-management-with-react (use memo and dispatcher)
https://qwik.builder.io/
Elm
Next.js

Database:
Cassandra
MongoDB
DynamoDB

Build:
https://docs.docker.com/build/bake/

Improvements:
https://mvnrepository.com/artifact/org.keycloak

Grafana LOKI: Logs
Grafana: metrics
Grafana TEMPO: distributed tracing backend
Promteheus: Metrics
https://grafana.com/oss/tempo/
Profiling: - https://www.cncf.io/blog/2022/05/31/what-is-continuous-profiling/
- https://grafana.com/blog/2022/11/02/announcing-grafana-phlare-oss-continuous-profiling-database/
- https://techcrunch.com/2023/03/15/grafana-acquires-pyroscope-and-merges-it-with-its-phlare-continuous-profiling-database
- https://github.com/grafana/faro-web-sdk/blob/main/packages/react/README.md
