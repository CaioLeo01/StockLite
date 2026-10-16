# StockLite

A REST API for inventory management built with Java 21 and Spring Boot. It supports product management, stock movements, user authentication, and role-based access, with a layered design that separates business rules from web and persistence details.

> **Project context:** StockLite is an academic portfolio project. The repository demonstrates backend design and implementation practices; it does not claim production usage or measured business impact.

## The problem and solution

Inventory systems need to track more than product records. They must control who can change the catalog, record who moved stock, and prevent a stock exit from exceeding the available quantity.

StockLite addresses this with a REST API for users, products, and stock movements. Product and user deactivation is logical, and each stock movement records its type, quantity, date, product, and responsible user.

## Features

- User registration and login with JWT authentication.
- Role-based access for ADMIN, ESTOQUISTA, and VISUALIZADOR.
- Product creation, listing, detail lookup, update, and logical deactivation.
- Stock entry and exit records, with a sequential stock-availability check for exits.
- Movement history and user/profile management.
- Centralized API error responses and request validation.
- OpenAPI documentation, when explicitly enabled in the environment.

Public registration assigns the VISUALIZADOR role. The product list filters inactive products; lookup by product ID can return an inactive product.

## Architecture

The code separates the application into four areas:

- **Domain:** business models and repository contracts.
- **Application:** use cases, DTOs, ports, and business exceptions.
- **Presentation:** REST controllers and centralized exception handling.
- **Infrastructure:** JPA persistence, database adapters, configuration, and JWT security.

Dependencies point inward: application use cases depend on domain contracts, while infrastructure implements those contracts. Controllers translate HTTP requests and responses; the business rules remain in use cases.

## Why this stack fits the project

The repository does not record the original selection history. The rationale below explains how the current technologies fit the implementation; it should not be read as a claim about the original decision-maker's motivation.

| Technology | Why it fits this project |
| --- | --- |
| **Java 21** | Provides a statically typed, object-oriented foundation for expressing inventory rules and keeping a maintainable backend codebase. |
| **Spring Boot and Spring MVC** | Provide the web application structure, dependency injection, REST endpoints, validation, and integration with persistence and security used by the API. |
| **Spring Security and JWT** | Support stateless authentication and role-based authorization for the ADMIN, ESTOQUISTA, and VISUALIZADOR profiles. Passwords are hashed with BCrypt. |
| **PostgreSQL** | Fits the relational data model: users, profiles, products, and movements have defined relationships and constraints. The migrations use foreign keys, unique indexes, and checks. |
| **Spring Data JPA** | Provides repository-based persistence for the relational model, while adapters keep framework-specific persistence behind domain contracts. |
| **Flyway** | Keeps database schema changes in ordered, version-controlled migrations rather than relying on automatic Hibernate schema generation. |
| **Maven Wrapper** | Lets contributors use the project's Maven build without requiring a separately installed Maven version. |
| **Docker** | Packages the API in a repeatable image; the Dockerfile builds with Maven and runs on a Java 21 runtime image. |
| **SpringDoc OpenAPI** | Provides an OpenAPI specification and Swagger UI for exploring and testing the HTTP API during development. |

## Main API routes

All routes use the default context path /v1/api. Protected routes require a valid JWT.

| Route | Purpose | Allowed profiles |
| --- | --- | --- |
| POST /auth/register | Register a user; the assigned profile is VISUALIZADOR | Public |
| POST /auth/login | Authenticate and obtain a token | Public |
| GET /health | Check service health | Public |
| GET /perfis | List access profiles | ADMIN |
| GET /usuarios | List users | ADMIN |
| PUT /usuarios/{id} | Update a user | ADMIN |
| DELETE /usuarios/{id} | Deactivate a user | ADMIN |
| POST /produtos | Create a product | ADMIN, ESTOQUISTA |
| GET /produtos and GET /produtos/{id} | List or view products | All profiles |
| PUT /produtos/{id} and DELETE /produtos/{id} | Update or deactivate a product | ADMIN, ESTOQUISTA |
| GET /movimentacoes | View stock movement history | All profiles |
| POST /movimentacoes/entrada and POST /movimentacoes/saida | Record a stock entry or exit | ADMIN, ESTOQUISTA |

## Technology versions

Versions are defined in pom.xml and Dockerfile.

| Component | Version or source |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.6 |
| JJWT | 0.11.5 |
| SpringDoc OpenAPI | 3.0.3 |
| PostgreSQL | Version selected by the database environment |
| Maven | Maven Wrapper configuration |
| Docker runtime | eclipse-temurin:21-jre |

## Run locally

### Prerequisites

- Java 21
- PostgreSQL
- Environment variables for database access and the JWT signing secret

Set the required environment variables and enable API documentation for local development:

~~~bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/stocklite
export DB_USERNAME=postgres
export DB_PASSWORD=your_local_password
export APP_JWT_SECRET=replace_with_a_long_random_secret
export SPRINGDOC_SWAGGER_UI_ENABLED=true
export SPRINGDOC_API_DOCS_ENABLED=true

./mvnw spring-boot:run
~~~

The API is available at http://localhost:8080/v1/api.

To build and run the automated tests:

~~~bash
./mvnw clean package
./mvnw test
~~~

### Run with Docker

Build the image:

~~~bash
docker build -t stocklite .
~~~

Run it by passing the database and JWT settings:

~~~bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host:5432/stocklite \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your_local_password \
  -e APP_JWT_SECRET=replace_with_a_long_random_secret \
  stocklite
~~~

## Configuration

| Variable | Required | Purpose |
| --- | --- | --- |
| SPRING_PROFILES_ACTIVE | No | Active Spring profile; defaults to dev |
| PORT or SERVER_PORT | No | HTTP port; defaults to 8080 |
| DB_URL | Yes | PostgreSQL JDBC URL |
| DB_USERNAME | Yes | Database user |
| DB_PASSWORD | Yes | Database password |
| APP_JWT_SECRET | Yes | Secret used to sign JWTs |
| APP_JWT_EXPIRATION_SECONDS | No | Token lifetime; defaults to 3600 seconds |
| SPRINGDOC_SWAGGER_UI_ENABLED | No | Enables Swagger UI; defaults to false |
| SPRINGDOC_API_DOCS_ENABLED | No | Enables the OpenAPI endpoint; defaults to false |
| APP_DB_SCHEMA | No | Schema setting defaults to stocklite; current entity mappings and migrations use the fixed stocklite schema |

Additional settings for CORS, JPA, and Flyway are available in application.properties. Never commit real credentials or JWT secrets.

## API documentation

Swagger UI and the OpenAPI JSON endpoint are disabled by default, including when the dev profile is active. Enable both environment variables shown in the local setup section to use them:

- Swagger UI: http://localhost:8080/v1/api/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v1/api/v3/api-docs

See [the development Swagger guide](docs/swagger-dev.md) for details.

## Database migrations

Flyway migrations are stored in src/main/resources/db/migration. They define the profile, user, product, and stock-movement tables, including foreign keys and data-integrity constraints.

The repository also includes [conceptual and logical data-model diagrams](docs/Modelagem_Banco/) and example SQL scripts in docs/sql/.

## Testing and current scope

Automated tests cover REST controllers, application use cases, and JWT security. The repository also has a GitHub Actions workflow that runs tests before a tag-based Railway deployment.

No production adoption, performance benchmark, or measured business outcome is documented. Add quantified results only after collecting reproducible measurements.

## Further improvements

- Add a CI workflow that runs tests on pull requests and updates to the main branch.
- Add database-level locking or optimistic concurrency control for simultaneous stock movements, with a concurrency test.
- Add request/response examples for the main API operations.
