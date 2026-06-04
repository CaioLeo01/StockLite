# Deploy no Railway por Tag

Este projeto esta preparado para deploy no Railway via `GitHub Actions` sempre que uma nova tag no formato exato `homol00.00.00` for criada, por exemplo `homol01.01.01`.

## O que foi preparado no repositorio

- `Dockerfile`: build e empacotamento da aplicacao com Java 21.
- `.github/workflows/deploy-railway-tag.yml`: pipeline que testa e publica no Railway quando uma tag e criada.
- `application.properties`: aceita a porta `PORT` fornecida pelo Railway.

## Fluxo recomendado

1. Criar um projeto no Railway.
2. Criar um servico PostgreSQL no Railway.
3. Criar o servico da API no Railway.
4. Configurar as variaveis da API.
5. Criar um `Project Token` no Railway para o ambiente de homologacao.
6. Salvar os segredos no GitHub.
7. Criar uma tag no GitHub para disparar o deploy.

## Variaveis da aplicacao

Use o `src/main/resources/application.properties` como referencia das variaveis esperadas pela aplicacao. As principais sao:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_DB_SCHEMA`
- `APP_JWT_SECRET`

Esses valores reais nao devem ficar versionados. Configure-os diretamente no Railway e, se precisar no pipeline, use `GitHub Secrets`.

## Segredos do GitHub Actions

No repositorio do GitHub, em `Settings > Secrets and variables > Actions`, crie:

- `RAILWAY_TOKEN`
- `RAILWAY_PROJECT_ID`
- `RAILWAY_ENVIRONMENT_NAME`
- `RAILWAY_SERVICE_NAME`

## Disparo do deploy

Exemplo de comandos:

```bash
git tag homol01.01.01
git push origin homol01.01.01
```

## Regra da tag

O workflow so faz deploy quando:

- a tag segue exatamente o formato `homol00.00.00`;
- a tag atual e maior que a maior tag `homol` valida ja existente no repositorio.

Exemplos validos:

- `homol01.00.00`
- `homol01.01.01`

Exemplos rejeitados:

- `homol1.1.1`
- `homol01.1.01`
- `homol01.01`
- `homol00.99.99` depois de ja existir `homol01.00.00`

Quando a tag for enviada, o workflow:

1. faz checkout completo do repositorio e das tags;
2. valida o formato e a ordem da nova tag;
3. sobe Java 21;
4. roda `./mvnw test`;
5. usa a imagem oficial `ghcr.io/railwayapp/cli:latest` para executar a Railway CLI;
6. executa `railway up` no servico configurado.

## Healthcheck sugerido no Railway

Configure o healthcheck da API para:

- Caminho: `/v1/api/health`

## Observacao importante sobre segredos

Nao publique credenciais reais em `.env`, prints ou documentacao. Para este fluxo, mantenha os valores sensiveis no Railway e no GitHub Secrets. Se alguma credencial real ja foi compartilhada, rotacione:

- senha do banco;
- segredo JWT;
- senha SMTP.
