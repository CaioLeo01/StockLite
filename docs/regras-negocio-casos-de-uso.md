# Regras de Negocio e Casos de Uso do StockLite

## Objetivo

Este documento foi refeito com base no projeto como um todo: controllers, services, DTOs, models, exceptions, configuracao de seguranca, entidades de persistencia, migrations, testes e arquivos de configuracao. O conteudo abaixo descreve apenas comportamentos efetivamente implementados.

## Escopo Analisado

- Camada de apresentacao da API
- Casos de uso da camada `application`
- Modelos e contratos de repositorio do dominio
- Tratamento global de erros
- Configuracao de seguranca e autenticacao JWT
- Entidades JPA e migration de unicidade de produto
- Configuracao da aplicacao e OpenAPI
- Testes de casos de uso representativos

## Visao Geral do Sistema

O StockLite e uma API de controle de estoque. O sistema permite:

- registrar e autenticar usuarios
- cadastrar, consultar, atualizar e inativar produtos
- registrar entradas e saidas de estoque
- consultar o historico de movimentacoes
- listar usuarios e perfis
- inativar e atualizar usuarios
- verificar a saude operacional da aplicacao

O caminho base padrao da API e `/v1/api`.

## Perfis Existentes

Os perfis existentes no projeto sao:

- `ADMIN`
- `ESTOQUISTA`
- `VISUALIZADOR`

Os significados documentados no projeto sao:

- `ADMIN`: acesso completo ao sistema
- `ESTOQUISTA`: controle e movimentacao de estoque
- `VISUALIZADOR`: permissao apenas para consulta

## Regras Gerais do Sistema

- A aplicacao usa autenticacao por token JWT no header `Authorization`.
- O formato esperado e `Bearer <token>`.
- Endpoints protegidos exigem autenticacao.
- O sistema e stateless, sem sessao de servidor.
- O usuario autenticado precisa existir e estar ativo para executar operacoes protegidas.
- Um token invalido, ausente ou malformado em endpoint protegido gera `401 Unauthorized`.
- Falta de permissao para a acao gera `403 Forbidden`.
- A API padroniza erros no formato `{ "mensagem": "..." }`.
- Validacoes de entrada com falha geram `400 Bad Request`.
- O Swagger/OpenAPI fica habilitado apenas no profile `dev`.

## Regras de Dominio Consolidadas

- Produto possui `idProduto`, `nome`, `descricao`, `preco`, `quantidadeEstoque` e `ativo`.
- Usuario possui `idUsuario`, `perfil`, `nome`, `email`, `senha` e `ativo`.
- Perfil possui `idPerfil`, `nome` e `descricao`.
- Movimentacao de estoque registra produto, usuario, tipo, quantidade, data de movimentacao e observacao.
- Produto e usuario nao sao removidos fisicamente pelos casos de uso implementados; ambos sao inativados logicamente.
- Um produto e considerado inativo quando `ativo` nao e `true`.
- Um usuario e considerado inativo quando `ativo` nao e `true`.
- O nome do produto deve ser unico sem diferenciar maiusculas e minusculas.
- A migration `V6__add_unique_index_to_produto_nome.sql` reforca a unicidade do nome do produto em banco com `LOWER(BTRIM(nome))`.
- O email do usuario deve ser unico sem diferenciar maiusculas e minusculas.
- Operacoes protegidas dependem da validacao do usuario autenticado no banco, nao apenas do conteudo do token.

## Casos de Uso

### 1. Registrar usuario

- Endpoint: `POST /auth/register`
- Resposta de sucesso: `201 Created`
- Objetivo: criar uma conta de usuario ativa no sistema

Entradas validadas:

- `nome` obrigatorio, entre 3 e 100 caracteres
- `email` obrigatorio, valido e com no maximo 100 caracteres
- `senha` obrigatoria, entre 8 e 100 caracteres
- `senha` deve conter letra maiuscula, minuscula, numero e caractere especial

Regras de negocio:

- O email informado e normalizado com `trim` e `lowercase`.
- Nao pode existir usuario com o mesmo email.
- O perfil atribuido no cadastro publico e sempre `VISUALIZADOR`.
- O perfil `VISUALIZADOR` precisa existir em base para o cadastro funcionar.
- A senha e armazenada com hash.
- O usuario e salvo como ativo.

Saida:

- retorna `idUsuario`, `nome`, `email` e mensagem de sucesso

Falhas implementadas:

- email ja em uso: `409 Conflict`
- perfil padrao inexistente: `500 Internal Server Error`
- validacao de entrada: `400 Bad Request`

### 2. Autenticar usuario

- Endpoint: `POST /auth/login`
- Resposta de sucesso: `200 OK`
- Objetivo: autenticar um usuario e emitir JWT

Entradas validadas:

- `email` obrigatorio, valido e com no maximo 100 caracteres
- `senha` obrigatoria e com no maximo 100 caracteres

Regras de negocio:

- O email informado e normalizado com `trim` e `lowercase`.
- O login falha se o email nao existir.
- O login falha se a senha nao corresponder ao hash salvo.
- Usuario inativo nao pode acessar o sistema.
- Usuario sem perfil valido nao pode acessar o sistema.
- Em caso de sucesso, o token contem:
- subject com o `idUsuario`
- claim `email`
- claim `nivelPermissao`
- data de expiracao configurada em `app.jwt.expiration-seconds`

Saida:

- retorna `token`

Falhas implementadas:

- credenciais invalidas: `401 Unauthorized`
- usuario inativo ou sem perfil valido: `403 Forbidden`
- validacao de entrada: `400 Bad Request`

### 3. Cadastrar produto

- Endpoint: `POST /produtos`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`
- Resposta de sucesso: `201 Created`
- Objetivo: criar um novo produto de estoque

Entradas validadas:

- `nome` obrigatorio, entre 3 e 100 caracteres
- `descricao` opcional, com no maximo 255 caracteres
- `preco` obrigatorio e maior que zero
- `quantidadeInicial` obrigatoria e maior ou igual a zero

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O nome e normalizado com `trim`.
- A descricao e normalizada com `trim`.
- Descricao em branco e convertida para `null`.
- Nao pode existir outro produto com o mesmo nome, ignorando diferenca de caixa.
- O produto e criado com `ativo = true`.
- A quantidade inicial informada torna-se o estoque inicial do produto.

Saida:

- retorna `idProduto`, `nome`, `descricao`, `preco`, `quantidadeAtual` e `status`

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto duplicado: `409 Conflict`
- validacao de entrada: `400 Bad Request`

### 4. Listar produtos

- Endpoint: `GET /produtos`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- Resposta de sucesso: `200 OK`
- Objetivo: consultar produtos ativos

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- A listagem filtra produtos inativos.
- A resposta da listagem expone apenas `idProduto`, `nome` e `preco`.

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`

### 5. Buscar produto por id

- Endpoint: `GET /produtos/{id}`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- Resposta de sucesso: `200 OK`
- Objetivo: consultar detalhes de um produto especifico

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O produto precisa existir.
- O caso de uso retorna o produto mesmo se ele estiver inativo.
- A resposta expone `idProduto`, `nome`, `descricao`, `quantidadeEstoque`, `preco` e `status`.

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto nao encontrado: `404 Not Found`
- identificador invalido no path: `400 Bad Request`

### 6. Atualizar produto

- Endpoint: `PUT /produtos/{id}`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`
- Resposta de sucesso: `200 OK`
- Objetivo: alterar os dados cadastrais e o estoque atual do produto

Entradas validadas:

- `nome` obrigatorio, entre 3 e 100 caracteres
- `descricao` opcional, com no maximo 255 caracteres
- `preco` obrigatorio e maior que zero
- `quantidadeEstoque` obrigatoria e maior ou igual a zero

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O produto precisa existir.
- O nome e normalizado com `trim`.
- A descricao e normalizada com `trim`.
- Descricao em branco e convertida para `null`.
- Nao pode haver outro produto com o mesmo nome, ignorando diferenca de caixa.
- O proprio produto pode manter seu nome atual sem conflito.
- O caso de uso permite alterar diretamente a quantidade em estoque.

Saida:

- retorna mensagem de sucesso

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto nao encontrado: `404 Not Found`
- conflito de nome: `409 Conflict`
- validacao de entrada: `400 Bad Request`

### 7. Inativar produto

- Endpoint: `DELETE /produtos/{id}`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`
- Resposta de sucesso: `200 OK`
- Objetivo: realizar exclusao logica de produto

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O produto precisa existir.
- O caso de uso nao remove o registro fisicamente.
- Se o produto ja estiver inativo, a operacao nao falha; ela retorna mensagem especifica.
- Se o produto estiver ativo, ele e marcado com `ativo = false`.

Saida:

- retorna mensagem de sucesso ou mensagem informando que o produto ja estava inativo

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto nao encontrado: `404 Not Found`

### 8. Registrar entrada de estoque

- Endpoint: `POST /movimentacoes/entrada`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`
- Resposta de sucesso: `201 Created`
- Objetivo: acrescentar unidades ao estoque e registrar a movimentacao

Entradas validadas:

- `produtoId` obrigatorio
- `quantidade` obrigatoria e maior que zero
- `observacao` opcional, com no maximo 255 caracteres

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O produto precisa existir.
- Produto inativo nao pode receber movimentacao.
- A observacao e normalizada com `trim`.
- Observacao em branco e convertida para `null`.
- O estoque do produto e incrementado pela quantidade informada.
- O sistema registra uma movimentacao do tipo `ENTRADA`.
- A movimentacao grava produto, usuario responsavel, tipo, quantidade, data/hora e observacao.
- O metodo e transacional: atualizacao de estoque e gravacao da movimentacao fazem parte da mesma unidade de trabalho.

Saida:

- retorna `idMovimentacao`, `produtoId`, `produtoNome` e `quantidadeAtual`

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto nao encontrado: `404 Not Found`
- produto inativo para movimentacao: `403 Forbidden`
- validacao de entrada: `400 Bad Request`

### 9. Registrar saida de estoque

- Endpoint: `POST /movimentacoes/saida`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`
- Resposta de sucesso: `201 Created`
- Objetivo: reduzir unidades do estoque e registrar a movimentacao

Entradas validadas:

- `produtoId` obrigatorio
- `quantidade` obrigatoria e maior que zero
- `observacao` opcional, com no maximo 255 caracteres

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O produto precisa existir.
- Produto inativo nao pode receber movimentacao.
- A observacao e normalizada com `trim`.
- Observacao em branco e convertida para `null`.
- A saida so pode acontecer se houver estoque suficiente.
- O estoque do produto e decrementado pela quantidade informada.
- O sistema registra uma movimentacao do tipo `SAIDA`.
- A movimentacao grava produto, usuario responsavel, tipo, quantidade, data/hora e observacao.
- O metodo e transacional: atualizacao de estoque e gravacao da movimentacao fazem parte da mesma unidade de trabalho.

Saida:

- retorna `idMovimentacao`, `produtoId`, `produtoNome` e `quantidadeAtual`

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- produto nao encontrado: `404 Not Found`
- produto inativo para movimentacao: `403 Forbidden`
- estoque insuficiente: `409 Conflict`
- validacao de entrada: `400 Bad Request`

### 10. Listar movimentacoes

- Endpoint: `GET /movimentacoes`
- Perfis autorizados: `ADMIN`, `ESTOQUISTA`, `VISUALIZADOR`
- Resposta de sucesso: `200 OK`
- Objetivo: consultar o historico de movimentacoes de estoque

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- As movimentacoes sao retornadas em ordem decrescente de `dataMovimentacao`.
- Cada item retorna `idMovimentacao`, `produtoId`, `produtoNome`, `usuarioId`, `usuarioNome`, `tipo`, `quantidade`, `dataMovimentacao` e `observacao`.

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`

### 11. Listar usuarios

- Endpoint: `GET /usuarios`
- Perfis autorizados: `ADMIN`
- Resposta de sucesso: `200 OK`
- Objetivo: consultar usuarios ativos do sistema

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- A listagem filtra usuarios inativos.
- A resposta retorna `idUsuario`, `nome`, `email`, `perfilId`, `perfilNome` e `status`.

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- usuario sem permissao: `403 Forbidden`

### 12. Atualizar usuario

- Endpoint: `PUT /usuarios/{id}`
- Perfis autorizados: `ADMIN`
- Resposta de sucesso: `200 OK`
- Objetivo: alterar dados cadastrais, perfil e status de um usuario

Entradas validadas:

- `nome` obrigatorio, entre 3 e 100 caracteres
- `email` obrigatorio, valido e com no maximo 100 caracteres
- `perfilId` obrigatorio
- `status` obrigatorio

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O administrador autenticado nao pode atualizar o proprio usuario por este caso de uso.
- O usuario alvo precisa existir.
- O perfil informado precisa existir.
- O email e normalizado com `trim` e `lowercase`.
- Nao pode haver outro usuario com o mesmo email.
- O proprio usuario pode manter seu email atual sem conflito.
- O caso de uso permite trocar o status ativo/inativo do usuario alvo.

Saida:

- retorna mensagem de sucesso

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- tentativa de autoatualizacao: `403 Forbidden`
- usuario alvo inexistente: `404 Not Found`
- perfil inexistente: `404 Not Found`
- conflito de email: `409 Conflict`
- validacao de entrada: `400 Bad Request`

### 13. Inativar usuario

- Endpoint: `DELETE /usuarios/{id}`
- Perfis autorizados: `ADMIN`
- Resposta de sucesso: `200 OK`
- Objetivo: realizar exclusao logica de usuario

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- O administrador autenticado nao pode inativar a propria conta por este caso de uso.
- O usuario alvo precisa existir.
- O caso de uso nao remove o registro fisicamente.
- Se o usuario ja estiver inativo, a operacao nao falha; ela retorna mensagem especifica.
- Se o usuario estiver ativo, ele e marcado com `ativo = false`.

Saida:

- retorna mensagem de sucesso ou mensagem informando que o usuario ja estava inativo

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- tentativa de autoexclusao logica: `403 Forbidden`
- usuario alvo inexistente: `404 Not Found`

### 14. Listar perfis

- Endpoint: `GET /perfis`
- Perfis autorizados: `ADMIN`
- Resposta de sucesso: `200 OK`
- Objetivo: consultar os perfis cadastrados

Regras de negocio:

- O usuario autenticado precisa existir e estar ativo.
- Cada perfil retorna `idPerfil`, `nome` e `descricao`.

Falhas implementadas:

- usuario autenticado inexistente ou inativo: `403 Forbidden`
- usuario sem permissao: `403 Forbidden`

### 15. Consultar saude da aplicacao

- Endpoint: `GET /health`
- URL final padrao: `GET /v1/api/health`
- Resposta de sucesso: `200 OK`
- Objetivo: verificar rapidamente a saude operacional da API

Regras implementadas:

- o endpoint e publico
- a resposta retorna `status`, `dataHora` e `tempoRespostaSegundos`
- o status retornado pelo service e `UP`
- o tempo de resposta e calculado a partir do tempo gasto para montar a resposta

## Matriz de Permissao por Modulo

- `ADMIN`: pode autenticar, consultar produtos, cadastrar produto, atualizar produto, inativar produto, registrar entrada, registrar saida, listar movimentacoes, listar usuarios, atualizar usuario, inativar usuario, listar perfis e consultar health
- `ESTOQUISTA`: pode autenticar, consultar produtos, cadastrar produto, atualizar produto, inativar produto, registrar entrada, registrar saida, listar movimentacoes e consultar health
- `VISUALIZADOR`: pode autenticar, listar produtos, consultar produto por id, listar movimentacoes e consultar health

## Comportamento HTTP e de Erro

### Formato padrao de erro

- corpo: `{ "mensagem": "..." }`

### Erros de autenticacao e autorizacao

- token invalido ou nao informado em endpoint protegido: `401 Unauthorized`
- usuario sem permissao para executar a acao: `403 Forbidden`
- usuario autenticado inexistente ou inativo: `403 Forbidden`
- usuario inativo ou sem perfil valido no login: `403 Forbidden`

### Erros funcionais mapeados

- credenciais invalidas: `401 Unauthorized`
- produto nao encontrado: `404 Not Found`
- usuario nao encontrado: `404 Not Found`
- perfil nao encontrado: `404 Not Found`
- produto duplicado: `409 Conflict`
- email duplicado no cadastro: `409 Conflict`
- conflito de email na atualizacao: `409 Conflict`
- estoque insuficiente: `409 Conflict`
- movimentacao em produto inativo: `403 Forbidden`
- identificador de produto invalido no path: `400 Bad Request`
- recurso inexistente: `404 Not Found`

### Validacoes de entrada

- erros de bean validation sao concatenados em uma unica mensagem
- entradas invalidas retornam `400 Bad Request`

## Regras Tecnicas com Impacto Funcional

- O JWT usa algoritmo `HS256`.
- O token carrega `idUsuario`, `email` e `nivelPermissao`.
- A seguranca usa `@PreAuthorize` nos controllers para restringir perfis por caso de uso.
- A configuracao de seguranca tambem exige autenticacao por rota HTTP para produtos, movimentacoes, usuarios e perfis.
- O sistema desabilita `csrf`, `httpBasic`, `formLogin` e `logout`.
- O profile padrao da aplicacao e `dev`, salvo sobrescrita por variavel de ambiente.
- A porta padrao da API e `8080`, salvo sobrescrita por variavel de ambiente.
- O contexto padrao da API e `/v1/api`, salvo sobrescrita por variavel de ambiente.
- O tempo de expiracao do token e configuravel por `APP_JWT_EXPIRATION_SECONDS`, com padrao de `3600` segundos.

## Observacoes de Escopo

- Este documento nao descreve requisitos desejados ou hipoteticos.
- Este documento nao presume regras que nao estejam implementadas no codigo analisado.
- Quando uma regra aparece aqui, ela foi identificada diretamente no comportamento do projeto.
