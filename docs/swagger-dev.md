# Swagger em Desenvolvimento

Este projeto habilita o Swagger apenas no profile `dev`.

## Como acessar

1. Inicie a aplicacao com o profile de desenvolvimento ativo.
2. Abra a interface do Swagger no navegador.

Link em `dev`:

- Swagger UI: `http://localhost:8080/v1/api/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v1/api/v3/api-docs`

## Como subir a aplicacao em dev

O `application.properties` ja define `dev` como profile padrao via `spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`.

Para executar localmente:

```bash
./mvnw spring-boot:run
```

Se quiser forcar explicitamente o profile `dev`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## O que fica habilitado nesse ambiente

- `springdoc.swagger-ui.enabled=true`
- `springdoc.api-docs.enabled=true`
- `server.port=8080`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3`

## Observacoes de uso

- A interface do Swagger so deve responder quando a aplicacao estiver no profile `dev`.
- Se a porta for alterada pela variavel `SERVER_PORT`, ajuste o link trocando `8080` pela porta configurada.
- O endpoint `/v3/api-docs` pode ser usado para integracoes, testes e importacao da especificacao OpenAPI.
- Se o navegador continuar retornando `404`, confirme se as dependencias Maven foram baixadas e se a aplicacao foi reiniciada apos a inclusao do starter do SpringDoc.
- Nao use `/swagger-ui/index.html` como URL principal neste projeto. Use `/swagger-ui.html`.
- Se `/v3/api-docs` abrir e `/swagger-ui.html` nao abrir, a aplicacao provavelmente ainda nao foi reiniciada com a nova dependencia.
