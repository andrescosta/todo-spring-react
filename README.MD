# Project TODO

## Architecture
![Alt](docs/arch.drawio.png)

## Datamodel
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
### Services
* Java 19
* Spring Boot
### Authentication
* KeyCloak (SSO)
  * OpenID Connect

### Database
* Postgress

#### Helpers
* Error handling
https://github.com/wimdeblauwe/error-handling-spring-boot-starter

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

## Web
* React

## Documentation:
* Asciidoctor

## Container
* Docker

## Test Instructions

### Deployment

docker compose up -d

### Application 

URL: http://localhost:3000/ 

User: test/123

### Grafana
http://localhost:3030/

#### Dashboards
- JVM (Micrometer)
- Logs, Traces, Metrics
- PostgreSQL Database

### Keycloak
http://localhost:8282/admin/

### PosgresSQL
Host: localhost:5432

Database: todo





