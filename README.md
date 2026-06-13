# StockLite

API academica para controle de estoque, desenvolvida com Java e Spring Boot, com foco em Programacao Orientada a Objetos, autenticacao JWT e organizacao baseada em Arquitetura Limpa.

O sistema foi proposto para permitir cadastro de usuarios, controle de produtos, registro e consulta de movimentacoes de estoque, com separacao clara de responsabilidades entre dominio, casos de uso, interfaces de entrada e adaptadores de infraestrutura.

## Sumario

- [Visao geral](#visao-geral)
- [Objetivo do projeto](#objetivo-do-projeto)
- [Arquitetura](#arquitetura)
- [Estrutura de pacotes](#estrutura-de-pacotes)
- [Tecnologias e versoes](#tecnologias-e-versoes)
- [Funcionalidades](#funcionalidades)
- [Estado atual da implementacao](#estado-atual-da-implementacao)
- [Como executar](#como-executar)
- [Variaveis de ambiente](#variaveis-de-ambiente)
- [Documentacao da API](#documentacao-da-api)
- [Endpoints principais](#endpoints-principais)
- [Banco de dados e migracoes](#banco-de-dados-e-migracoes)
- [Testes](#testes)
- [Boas praticas adotadas](#boas-praticas-adotadas)
- [Melhorias recomendadas](#melhorias-recomendadas)

## Visao geral

O StockLite foi concebido como um sistema simples para pequenos estoques, setores internos e cenarios academicos. A aplicacao permite autenticar usuarios, controlar perfis de acesso, manter o cadastro de produtos e consultar o historico de movimentacoes.

Mais do que apenas expor CRUDs, o projeto busca demonstrar:

- aplicacao de conceitos de POO;
- separacao em camadas com fronteiras bem definidas;
- autenticacao e autorizacao com JWT;
- persistencia relacional com JPA;
- migracoes versionadas com Flyway;
- testes automatizados para controllers, seguranca e casos de uso.

## Objetivo do projeto

O objetivo do StockLite e apoiar o controle de estoque de forma objetiva, registrando produtos, usuarios responsaveis e operacoes realizadas no estoque.

Na proposta funcional do sistema, as regras centrais incluem:

- autenticacao obrigatoria para rotas protegidas;
- controle de acesso por perfil;
- exclusao logica de usuarios e produtos;
- validacao de estoque antes de uma saida;
- historico de movimentacoes para rastreabilidade.

## Arquitetura

Este projeto adota os principios de **Arquitetura Limpa**.

Na pratica, o codigo esta organizado assim:

- **Domain**: contem entidades e contratos centrais do negocio, como `Produto`, `Usuario`, `Perfil`, `MovimentacaoEstoque` e interfaces de repositorio.
- **Application**: contem casos de uso, DTOs, portas de servicos externos e excecoes de regra de negocio.
- **Presentation**: contem os controllers REST e o tratamento global de excecoes, sem concentrar regra de negocio.
- **Infrastructure**: contem implementacoes concretas de persistencia, mapeamento JPA, configuracao da aplicacao e seguranca JWT.

### Fluxo resumido

1. O controller recebe a requisicao HTTP.
2. O caso de uso da camada `application` valida o usuario autenticado e aplica as regras de negocio.
3. O caso de uso depende de contratos da camada interna, como repositorios e portas.
4. A camada `infrastructure` implementa esses contratos com Spring Data JPA, JWT e configuracoes tecnicas.
5. A resposta volta em formato de DTO pela camada `presentation`.

### Dependencias entre camadas

- `presentation` depende de `application`
- `application` depende de `domain` e de portas
- `infrastructure` implementa contratos usados por `application` e `domain`
- `domain` nao depende de frameworks

Essa organizacao facilita manutencao, testes e evolucao da aplicacao sem acoplamento excessivo a detalhes de framework.

## Estrutura de pacotes

```text
src/main/java/com/example/stocklite
├── application
│   ├── dto
│   ├── exception
│   ├── port
│   ├── security
│   └── usecase
├── domain
│   ├── model
│   └── repository
├── infrastructure
│   ├── config
│   ├── persistence
│   └── security
└── presentation
    ├── controller
    └── exception
```

## Tecnologias e versoes

As versoes abaixo foram extraidas do `pom.xml` e dos artefatos do projeto:

| Tecnologia | Versao |
| --- | --- |
| Java | 21 |
| Maven Wrapper | Maven 3.x via `./mvnw` |
| Spring Boot | 4.0.6 |
| Spring Web MVC | gerenciado pelo Spring Boot 4.0.6 |
| Spring Data JPA | gerenciado pelo Spring Boot 4.0.6 |
| Spring Security | gerenciado pelo Spring Boot 4.0.6 |
| Spring Validation | gerenciado pelo Spring Boot 4.0.6 |
| Flyway | gerenciado pelo Spring Boot 4.0.6 |
| PostgreSQL Driver | gerenciado pelo Spring Boot 4.0.6 |
| JJWT | 0.11.5 |
| SpringDoc OpenAPI UI | 3.0.3 |
| Lombok | gerenciado pelo Spring Boot 4.0.6 |
| Docker base image | `maven:3.9.11-eclipse-temurin-21` e `eclipse-temurin:21-jre` |

## Funcionalidades

### Funcionalidades previstas na proposta do sistema

- autenticacao de usuarios com JWT;
- cadastro de usuarios;
- controle de acesso por perfil;
- listagem, atualizacao e inativacao logica de usuarios;
- cadastro, consulta, atualizacao e inativacao logica de produtos;
- registro de entradas e saidas de estoque;
- validacao de estoque disponivel para saida;
- historico de movimentacoes.

### Perfis de acesso previstos

- `ADMIN`
- `ESTOQUISTA`
- `VISUALIZADOR`

## Estado atual da implementacao

Com base no codigo atual deste repositorio, ja estao implementados:

- `POST /auth/register`
- `POST /auth/login`
- `GET /health`
- `GET /perfis`
- `GET /usuarios`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`
- `POST /produtos`
- `GET /produtos`
- `GET /produtos/{id}`
- `PUT /produtos/{id}`
- `DELETE /produtos/{id}`
- `GET /movimentacoes`
- `POST /movimentacoes/entrada`
- `POST /movimentacoes/saida`

## Como executar

### Pre-requisitos

- Java 21
- `JAVA_HOME` configurado
- Maven Wrapper do projeto
- PostgreSQL disponivel

### Execucao local com Maven

1. Configure as variaveis de ambiente obrigatorias.
2. Suba um banco PostgreSQL acessivel pela aplicacao.
3. Execute:

```bash
./mvnw spring-boot:run
```

Por padrao, o profile ativo e `dev`.

Se quiser explicitar:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

API local padrao:

```text
http://localhost:8080/v1/api
```

Healthcheck:

```text
GET http://localhost:8080/v1/api/health
```

### Gerar o jar

```bash
./mvnw clean package
```

### Executar com Docker

Build da imagem:

```bash
docker build -t stocklite .
```

Execucao:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host:5432/database \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e APP_DB_SCHEMA=stocklite \
  -e APP_JWT_SECRET=troque_por_um_segredo_forte \
  stocklite
```

## Variaveis de ambiente

As configuracoes principais da aplicacao sao externalizadas via `application.properties`.

| Variavel | Obrigatoria | Descricao |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | nao | Profile ativo. Padrao: `dev` |
| `PORT` ou `SERVER_PORT` | nao | Porta HTTP. Padrao: `8080` |
| `SERVER_SERVLET_CONTEXT_PATH` | nao | Caminho base. Padrao: `/v1/api` |
| `DB_URL` | sim | URL JDBC do PostgreSQL |
| `DB_USERNAME` | sim | Usuario do banco |
| `DB_PASSWORD` | sim | Senha do banco |
| `DB_DRIVER_CLASS_NAME` | nao | Driver JDBC. Padrao: `org.postgresql.Driver` |
| `APP_DB_SCHEMA` | nao | Schema da aplicacao. Padrao: `stocklite` |
| `JPA_DDL_AUTO` | nao | Estrategia DDL do Hibernate. Padrao: `none` |
| `JPA_SHOW_SQL` | nao | Exibicao de SQL no log |
| `JPA_FORMAT_SQL` | nao | Formatacao de SQL no log |
| `FLYWAY_ENABLED` | nao | Habilita migracoes |
| `FLYWAY_CREATE_SCHEMAS` | nao | Permite criacao do schema |
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | nao | Origins permitidas |
| `APP_CORS_ALLOWED_METHODS` | nao | Metodos CORS |
| `APP_CORS_ALLOWED_HEADERS` | nao | Headers CORS |
| `APP_CORS_EXPOSED_HEADERS` | nao | Headers expostos |
| `APP_CORS_ALLOW_CREDENTIALS` | nao | Credenciais no CORS |
| `APP_CORS_MAX_AGE` | nao | Cache do preflight |
| `APP_JWT_SECRET` | sim | Segredo usado na assinatura do JWT |
| `APP_JWT_EXPIRATION_SECONDS` | nao | Expiracao do token. Padrao: `3600` |

### Exemplo recomendado de configuracao local

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/stocklite
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export APP_DB_SCHEMA=stocklite
export APP_JWT_SECRET=troque_por_um_segredo_forte_com_tamanho_adequado
```

## Documentacao da API

Quando o profile `dev` estiver ativo, a documentacao OpenAPI fica habilitada.

- Swagger UI: `http://localhost:8080/v1/api/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v1/api/v3/api-docs`

Documentos adicionais do projeto:

- [HELP.md](HELP.md)
- [docs/swagger-dev.md](docs/swagger-dev.md)
- [docs/health-endpoint.md](docs/health-endpoint.md)
- [docs/deploy-railway.md](docs/deploy-railway.md)

## Endpoints principais

### Autenticacao

- `POST /v1/api/auth/register`
- `POST /v1/api/auth/login`

### Health

- `GET /v1/api/health`

### Usuarios

- `GET /v1/api/usuarios` - `ADMIN`
- `PUT /v1/api/usuarios/{id}` - `ADMIN`
- `DELETE /v1/api/usuarios/{id}` - `ADMIN`

### Perfis

- `GET /v1/api/perfis` - `ADMIN`

### Produtos

- `POST /v1/api/produtos` - `ADMIN`, `ESTOQUISTA`
- `GET /v1/api/produtos` - `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- `GET /v1/api/produtos/{id}` - `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- `PUT /v1/api/produtos/{id}` - `ADMIN`, `ESTOQUISTA`
- `DELETE /v1/api/produtos/{id}` - `ADMIN`, `ESTOQUISTA`

### Movimentacoes

- `GET /v1/api/movimentacoes` - `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- `POST /v1/api/movimentacoes/entrada` - `ADMIN`, `ESTOQUISTA`
- `POST /v1/api/movimentacoes/saida` - `ADMIN`, `ESTOQUISTA`

## Banco de dados e migracoes

O projeto utiliza PostgreSQL com migracoes versionadas via Flyway.

Migracoes localizadas em:

```text
src/main/resources/db/migration
```

Arquivos atualmente presentes:

- `V1__init_stocklite_schema.sql`
- `V2__create_perfil_table.sql`
- `V3__create_usuario_table.sql`
- `V4__create_produto_table.sql`
- `V5__create_movimentacao_estoque_table.sql`
- `V6__add_unique_index_to_produto_nome.sql`

Arquivos auxiliares de carga inicial:

- `docs/sql/insert_perfis_iniciais.sql`
- `docs/sql/insert_produtos_exemplo.sql`

## Testes

O projeto possui atualmente **18 arquivos de teste** em `src/test/java`, cobrindo principalmente:

- controllers REST;
- casos de uso da camada `application`;
- servicos de seguranca JWT.

Comando esperado:

```bash
./mvnw test
```

Observacao: durante a geracao deste README, a execucao local dos testes nao foi concluida neste ambiente porque o `JAVA_HOME` nao estava configurado corretamente no shell utilizado.

## Boas praticas adotadas

- separacao de responsabilidades por camadas;
- casos de uso isolando regras de negocio;
- uso de interfaces para portas e repositorios;
- dependencia da regra de negocio para contratos, nao para frameworks;
- autenticacao stateless com JWT;
- autorizacao por perfil com `@PreAuthorize`;
- DTOs para entrada e saida da API;
- tratamento centralizado de excecoes;
- migracoes versionadas com Flyway;
- testes automatizados para comportamentos criticos;
- containerizacao com Docker;
- configuracao externalizada por variaveis de ambiente.

## Melhorias recomendadas

Como boas praticas adicionais para evolucao do projeto, vale considerar:

- nao versionar credenciais reais em arquivos de profile;
- adicionar um `.env.example` ou secao de bootstrap de ambiente local;
- criar endpoints explicitos para entrada e saida de estoque, alinhando implementacao e proposta funcional;
- incluir exemplos de payloads de requisicao e resposta;
- adicionar testes de integracao com banco e seguranca;
- configurar pipeline CI para validar build e testes em cada PR;
- documentar convencoes de commit, branching e versionamento;
- adicionar diagrama simples da arquitetura e do fluxo de autenticacao.

## Licenca

Este repositorio possui o arquivo [LICENSE](LICENSE). Se o projeto for entregue em contexto academico, ajuste esta secao conforme a politica da disciplina ou da instituicao.
