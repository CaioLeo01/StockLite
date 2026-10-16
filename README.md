# StockLite

A REST API for inventory management built with Java 21 and Spring Boot. It supports product management, stock movements, user authentication, and role-based access, with a layered design that separates business rules from web and persistence details.

> **Project context:** StockLite is an academic portfolio project. The repository demonstrates backend design and implementation practices; it does not claim production usage or measured business impact.

## The problem and solution

Inventory systems need to track more than product records. They must control who can change the catalog, record who moved stock, and check that enough stock is available when an exit is requested.

StockLite addresses this with a REST API for users, products, and stock movements. When recording a stock exit, it checks the available quantity and records the movement with its type, quantity, date, product, and responsible user. Product and user deactivation is logical.

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

### Why Clean Architecture?

I chose Clean Architecture to keep StockLite's business rules and use cases independent from HTTP controllers, database access, and framework details. In this project, the application layer depends on domain contracts, while infrastructure implements them. This separation makes the business logic easier to test and helps contain changes to external technologies within the adapters and configuration.

The trade-off is additional layers, interfaces, and code compared with a simpler layered CRUD application. For a small project, that structure can feel heavier than necessary. I chose it because StockLite is also meant to demonstrate separation of concerns and maintainable backend design; it is a design choice, not a claim that Clean Architecture is the best fit for every application.

## Technology choices and alternatives

These choices reflect the practical reasons for selecting the technologies for StockLite. The alternatives could also implement the same system; the trade-offs below explain why this stack was a suitable fit for the project's goals.

| Choice | Why I chose it | Alternative and trade-off |
| --- | --- | --- |
| **PostgreSQL** | I chose PostgreSQL because it is open source and does not require a paid database license, helping reduce licensing costs and giving the project flexibility as it evolves. Its relational model and integrity features fit StockLite's users, products, and stock movements, and provide a sound foundation as requirements grow. | **MySQL Community** is also free and open source, and can support this relational model. License cost alone does not distinguish the two, and this project has no benchmark proving one performs or scales better. Hosting, backups, and database operations can still have costs. |
| **Java 21** | I chose Java because its static typing and object-oriented structure help organize business rules into explicit, maintainable components. Static typing can catch some errors during compilation, and the language supports a clear structure for domain concepts such as products, users, profiles, and stock movements. | **C#/.NET** and other languages can offer similar object-oriented and static-typing features. Java can involve more code and structure than lighter alternatives; its fit here also reflects the project's layered design and my goal of demonstrating Java backend development. |
| **Spring Boot and Spring MVC** | I chose Spring Boot for its mature ecosystem, auto-configuration, and integrated support for building REST APIs and connecting common concerns such as security, validation, and persistence. This lets StockLite use a consistent set of tools without wiring each integration from scratch. | **Quarkus** and **Micronaut** are strong Java alternatives, with design choices that can favor build-time processing, fast startup, and lower memory use, especially in container or serverless environments. Spring Boot's main advantage for StockLite is its broad, cohesive ecosystem and straightforward integration with the libraries already used. The trade-off is that Quarkus or Micronaut may be a better fit when startup time or memory footprint is a primary constraint; native builds and build-time processing can also add build complexity. StockLite has no comparative benchmark, so this is a fit-based choice rather than a claim that Spring Boot is faster or universally better. |
| **Spring Data JPA** | Repository abstractions and entity mapping reduce repetitive persistence code for this relational model, while adapters keep persistence behind domain contracts. | Direct JDBC or SQL can offer more control over queries, but requires more persistence code. JPA still needs deliberate query design as the data size and performance requirements grow. |
| **JWT with Spring Security** | Signed tokens support stateless authentication and role-based access for the three profiles without a server-side session store. | Server-side sessions make permission changes and revocation easier to apply immediately, but require session storage and coordination. With JWT, a role change may not be reflected in an already issued token until it expires; the default lifetime is one hour. |
| **Flyway** | Versioned SQL migrations make schema changes explicit, ordered, and repeatable across environments. | Hibernate schema generation can be convenient during early development, but explicit migrations provide better control over database changes. |
| **Docker and Maven Wrapper** | The wrapper standardizes the project build command, and Docker packages the application in a Java 21 runtime image after a Maven build stage. | Running directly on a host avoids container setup, but can make environment differences harder to reproduce. |

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
