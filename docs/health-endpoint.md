# Endpoint de Saude

Este documento descreve exclusivamente o endpoint de monitoramento da aplicacao.

## Endpoint

- Metodo: `GET`
- URL: `http://localhost:8080/v1/api/health`

## Objetivo

Permitir a verificacao rapida da saude operacional da aplicacao.

## Caminho base da API

O prefixo da API vem da configuracao ativa da aplicacao:

- Propriedade: `server.servlet.context-path`
- Valor padrao: `/v1/api`

Com isso, o controller precisa declarar apenas `/health`, e a URL final continua sendo `GET /v1/api/health`.

## Resposta

O endpoint retorna:

- `status`: situacao atual da aplicacao. Neste caso, `UP`.
- `dataHora`: data e hora em que a resposta foi gerada.
- `tempoRespostaSegundos`: tempo de resposta medido em segundos.

## Exemplo de resposta

```json
{
  "status": "UP",
  "dataHora": "2026-05-28T10:30:00-03:00",
  "tempoRespostaSegundos": 0.002314
}
```
