# StockLite

A REST API for inventory management, built with Java 21 and Spring Boot. StockLite demonstrates how authentication, role-based access, stock movements, and persistence can be organized around clear application and domain boundaries.

> **Project context:** StockLite is an academic portfolio project. It demonstrates backend design and implementation practices; it is not presented as a production deployment.

## The problem

Small inventory workflows need more than product CRUD. They also need controlled access, reliable stock movement records, and a way to prevent outgoing movements from exceeding available stock. StockLite brings those responsibilities into one API.

## What it does

- Authenticates users with JWT.
- Applies role-based authorization for ADMIN, ESTOQUISTA, and VISUALIZADOR profiles.
- Creates, reads, updates, and logically deactivates products.
- Records stock entries and exits, checking available quantity before an exit.
- Keeps a history of stock movements.
- Manages users and profiles through restricted endpoints.
- Exposes API documentation with OpenAPI and Swagger.

## Architecture and key decisions

The code is organized into four areas:

- **Domain:** core models and repository contracts.
- **Application:** use cases, DTOs, ports, and business exceptions.
- **Presentation:** REST controllers and centralized exception handling.
- **Infrastructure:** persistence, JPA mappings, configuration, and JWT security.

The dependency direction keeps business rules centered in the domain and application layers. Infrastructure implements the contracts used by those layers, while controllers handle HTTP input and output.

Other implementation choices include PostgreSQL for relational persistence, Flyway for versioned schema migrations, and externalized configuration for database and JWT settings. The project includes a Dockerfile with a Maven build stage and a Java 21 runtime stage.

## Technology stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| API | Spring MVC, SpringDoc OpenAPI |
| Security | Spring Security, JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA, PostgreSQL |
| Database migrations | Flyway |
| Build | Maven Wrapper |
| Container | Docker |

Versions are taken from the repository's pom.xml and Dockerfile.

## Main API routes

All routes use the /v1/api context path. Protected routes require a valid JWT.

| Route | Purpose | Allowed profiles |
| --- | --- | --- |
| POST /auth/register | Register a user | Public |
| POST /auth/login | Authenticate and obtain a token | Public |
| GET /health | Check service health | Public |
| GET /perfis | List access profiles | ADMIN |
| GET /usuarios | List users | ADMIN |
| PUT /usuarios/{id} | Update a user | ADMIN |
| DELETE /usuarios/{id} | Deactivate a user | ADMIN |
| POST /produtos | Create a product | ADMIN, ESTOQUISTA |
| GET /produtos, GET /produtos/{id} | List or view products | All profiles |
| PUT /produtos/{id}, DELETE /produtos/{id} | Update or deactivate a product | ADMIN, ESTOQUISTA |
| GET /movimentacoes | View stock movement history | All profiles |
| POST /movimentacoes/entrada, POST /movimentacoes/saida | Record a stock entry or exit | ADMIN, ESTOQUISTA |

## Run locally

### Prerequisites

- Java 21
- PostgreSQL
- A configured database and the required environment variables

Set the database and JWT settings, then start the application:

~~~bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/stocklite
export DB_USERNAME=postgres
export DB_PASSWORD=your_local_password
export APP_DB_SCHEMA=stocklite
export APP_JWT_SECRET=replace_with_a_long_random_secret

./mvnw spring-boot:run
~~~

The API is available at http://localhost:8080/v1/api. Flyway migrations are stored in src/main/resources/db/migration.

To build and run the tests:

~~~bash
./mvnw clean package
./mvnw test
~~~

### Run with Docker

Build the image:

~~~bash
docker build -t stocklite .
~~~

Run it by passing the database and JWT environment variables to the container:

~~~bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host:5432/stocklite \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_local_password \
  -e APP_DB_SCHEMA=stocklite \
  -e APP_JWT_SECRET=replace_with_a_long_random_secret \
  stocklite
~~~

## Configuration

The application reads these settings from environment variables:

| Variable | Required | Purpose |
| --- | --- | --- |
| SPRING_PROFILES_ACTIVE | No | Active profile; defaults to dev |
| PORT or SERVER_PORT | No | HTTP port; defaults to 8080 |
| DB_URL | Yes | PostgreSQL JDBC URL |
| DB_USERNAME | Yes | Database user |
| DB_PASSWORD | Yes | Database password |
| APP_DB_SCHEMA | No | Database schema; defaults to stocklite |
| APP_JWT_SECRET | Yes | Secret used to sign JWTs |
| APP_JWT_EXPIRATION_SECONDS | No | Token lifetime; defaults to 3600 seconds |

Additional CORS, JPA, and Flyway settings are available in the application configuration.

## API documentation

With the dev profile active:

- Swagger UI: http://localhost:8080/v1/api/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v1/api/v3/api-docs

Additional project notes are available in docs/, including the deployment and Swagger guides.

## Testing and current scope

The repository contains automated tests for REST controllers, application use cases, and JWT security. Run them with ./mvnw test.

The project demonstrates design and implementation choices through its code and tests. No production usage or measured business impact is claimed.

## Possible next improvements

- Add measured test coverage and CI results to the project documentation.
- Expand request validation and API error examples.
- Add integration tests using a disposable PostgreSQL instance.
