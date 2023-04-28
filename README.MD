# Project: TODO App

## High level architecture
![Alt](docs/arch.drawio.png)

## Data Model
```mermaid
erDiagram
  MUser ||--o{ Activity : creates
  Activity ||--o{ Media : associates
  Activity ||--o{ Label_Activity : associates
  MUser ||--o{ Label: owns
  Label ||--o{ Label_Activity : associates

  MUser {
      bigint id PK
      uuid public_id UK
      text full_name
      text mail
      timestamptz created_at
      timestamptz updated_at
  }
  Activity {
    bigint id PK
    uuid public_id UK
    text name
    text description
    text type
    text status
    text state
    text[] tags
    jsonb extra_data
    bigint muser_id FK
    timestamptz created_at
    timestamptz updated_at
  }
  
  Label {
    bigint id PK
    uuid public_id UK
    text name
    text description
    bigint muser_id FK
    timestamptz created_at
    timestamptz updated_at
  }

  Media {
    bigint id PK
    uuid public_id UK
    bigint activity_id FK
    text name
    text type
    text uri
    text description
    json extra_data
    timestamptz created_at
    timestamptz updated_at
  }
  Label_Activity {
    bigint id PK
    bigint label_id FK
    bigint activity_id FK
    timestamptz created_at
    timestamptz updated_at
  }
```

## Stack

### Activity Service
* Java 19
* Spring Boot
* Spring RestDocs
* Micrometer
* Zipkin
* Loki4j
* Pyroscope
* Slf4j 
* Maven
* GSon
* JJWT


### Identity Management
* KeyCloak (SSO)
  * OpenID Connect

### Database
* Postgress

### API Documentation
* Asciidoctor

### Infra
* Nginx
* Docker

### Testing
* Junit 5 (Unit)
* TestContainers (Integration) 
* Gatling (Performance)

### Observability
> (metrics, logs, traces, continuous profiling)
* Prometheus
* Grafana 
* Loki  
* Tempo 
* Pyroscope
* Postgres-exporter

### Web Client
* React
* W3.CSS
* Babel
* Webpack
* Axios

## Commands

* Build locally
```
docker compose build
```

* Start the enviroment locally

```
docker compose up -d
```

* Run integration tests locally:

```
cd test
.\mvnw surefire:test
```

* Run performance tests locally:

```
cd .\performance\gatling\
.\mvnw gatling:test 
```

* API Documentation
```
cd .\todo-svc
.\mvnw package
```

## Local environment

* Home: http://localhost:3000/ 
  * User: test/123
* Grafana: http://localhost:3030/
  * User: admin/admin
  * Dashboards:
    * JVM (Micrometer)
    * Logs, Traces, Metrics
    * PostgreSQL Database
* Keycloak: http://localhost:8282/admin/
  * User: admin/admin
* PosgresSQL: localhost:5432
  * Database: todo
* API Docs
  * todo-svc\target\generated-docs\api-guide.html