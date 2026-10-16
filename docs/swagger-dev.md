# Swagger and OpenAPI in Development

Swagger UI and the OpenAPI JSON endpoint are disabled by default. Setting the Spring profile to dev alone does not enable them; the application properties default both switches to false.

## Enable the documentation endpoints

Set these environment variables before starting the application:

~~~bash
export SPRING_PROFILES_ACTIVE=dev
export SPRINGDOC_SWAGGER_UI_ENABLED=true
export SPRINGDOC_API_DOCS_ENABLED=true
./mvnw spring-boot:run
~~~

You can also configure the same variables in your local environment file, provided that file is not committed.

## Local URLs

With the default port and context path:

- Swagger UI: http://localhost:8080/v1/api/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v1/api/v3/api-docs

The context path defaults to /v1/api. If PORT or SERVER_PORT changes the port, replace 8080 in the URLs.

## Security note

Keep the Swagger UI and OpenAPI endpoints disabled in production unless you have an explicit operational reason to expose them. The application defaults both settings to false.

## Configuration reference

- springdoc.swagger-ui.enabled is controlled by SPRINGDOC_SWAGGER_UI_ENABLED.
- springdoc.api-docs.enabled is controlled by SPRINGDOC_API_DOCS_ENABLED.
- Both variables default to false in src/main/resources/application.properties.
