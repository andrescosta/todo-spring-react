# Project TODO

## Architecture
[TODO]

## Datamodel
```mermaid
erDiagram
  User ||--o{ Activity : creates
  Activity ||--o{ Media : associates
  Activity }o--o{ Label : associates

  User {
      int id PK
      string name
      string mail
  }
  Activity {
    int id PK
    int user_id FK
    int type
    int status
    string taks
    json extra_data
  }
  
  Label {
    int id PK
    string name
    string description
  }

  Media {
    int id PK
    int activity_id FK
    string type
    string URI
    string description
    json extra_data
  }
```

## Stack
### Services
* Java 19
* Spring Boot
### Authentication
* KeyCloak
  * OpenID Connect

### Database
* Postgress

#### Helpers
* Error handling
https://github.com/wimdeblauwe/error-handling-spring-boot-starter

### Testing
* Junit 5
* TestContainers 
* Gatling


### Observability
> (metrics, logs, traces, continuous profiling)
* Prometheus
* Grafana 
* Loki  
* Tempo 
* Pyroscope
* Postgres-exporter

## Web
* React

## Documentation:
* Asciidoctor

## Container
- Docker


## Future improvements
* Kafka and WebSockets
* Nginx
* JBang
* Thymeleaf + HTMX
* JDK FLight Recorder
* JDK Mission Control
* Visual VM 

## References
- https://spring.io/guides/topicals/spring-boot-docker/