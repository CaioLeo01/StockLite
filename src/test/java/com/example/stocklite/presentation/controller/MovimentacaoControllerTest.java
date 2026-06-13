package com.example.stocklite.presentation.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.dto.MovimentacaoResponse;
import com.example.stocklite.application.dto.RegistrarEntradaResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.InactiveProductMovementNotAllowedException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.ListarMovimentacoesService;
import com.example.stocklite.application.usecase.RegistrarEntradaEstoqueService;
import com.example.stocklite.domain.repository.MovimentacaoRepository;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataMovimentacaoRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataPerfilRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataProdutoRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataUsuarioRepository;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
class MovimentacaoControllerTest {

	private static final String CONTEXT_PATH = "/v1/api";
	private static final String TOKEN_VALIDO = "jwt-valido";
	private static final String TOKEN_INVALIDO = "jwt-invalido";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ListarMovimentacoesService listarMovimentacoesService;

	@MockitoBean
	private RegistrarEntradaEstoqueService registrarEntradaEstoqueService;

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private ProdutoRepository produtoRepository;

	@MockitoBean
	private PerfilRepository perfilRepository;

	@MockitoBean
	private MovimentacaoRepository movimentacaoRepository;

	@MockitoBean
	private SpringDataUsuarioRepository springDataUsuarioRepository;

	@MockitoBean
	private SpringDataPerfilRepository springDataPerfilRepository;

	@MockitoBean
	private SpringDataProdutoRepository springDataProdutoRepository;

	@MockitoBean
	private SpringDataMovimentacaoRepository springDataMovimentacaoRepository;

	@MockitoBean
	private PasswordHasher passwordHasher;

	@Test
	void deveListarMovimentacoesQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarMovimentacoesService.listar(usuarioAutenticado)).thenReturn(List.of(
				new MovimentacaoResponse(
						2,
						10,
						"Teclado mecanico",
						2,
						"Maria Souza",
						"SAIDA",
						5,
						OffsetDateTime.parse("2026-06-12T11:00:00Z"),
						"Retirada para uso interno")));

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].idMovimentacao").value(2))
				.andExpect(jsonPath("$[0].produtoId").value(10))
				.andExpect(jsonPath("$[0].produtoNome").value("Teclado mecanico"))
				.andExpect(jsonPath("$[0].usuarioId").value(2))
				.andExpect(jsonPath("$[0].usuarioNome").value("Maria Souza"))
				.andExpect(jsonPath("$[0].tipo").value("SAIDA"))
				.andExpect(jsonPath("$[0].quantidade").value(5))
				.andExpect(jsonPath("$[0].dataMovimentacao").value("2026-06-12T11:00:00Z"))
				.andExpect(jsonPath("$[0].observacao").value("Retirada para uso interno"));
	}

	@Test
	void deveListarMovimentacoesQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarMovimentacoesService.listar(usuarioAutenticado)).thenReturn(List.of(
				new MovimentacaoResponse(
						1,
						8,
						"Mouse gamer",
						2,
						"Maria Souza",
						"ENTRADA",
						10,
						OffsetDateTime.parse("2026-06-12T10:00:00Z"),
						null)));

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].produtoNome").value("Mouse gamer"))
				.andExpect(jsonPath("$[0].tipo").value("ENTRADA"));
	}

	@Test
	void deveListarMovimentacoesQuandoTokenForValidoEUsuarioForVisualizador() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarMovimentacoesService.listar(usuarioAutenticado)).thenReturn(List.of(
				new MovimentacaoResponse(
						3,
						15,
						"Monitor",
						1,
						"Joao da Silva",
						"SAIDA",
						1,
						OffsetDateTime.parse("2026-06-12T09:00:00Z"),
						"Uso interno")));

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].produtoNome").value("Monitor"));
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverMovimentacoes() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarMovimentacoesService.listar(usuarioAutenticado)).thenReturn(List.of());

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformado() throws Exception {
		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistente() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarMovimentacoesService.listar(usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(get("/v1/api/movimentacoes")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRegistrarEntradaQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(registrarEntradaEstoqueService.registrar(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new RegistrarEntradaResponse(1, 10, "Teclado mecanico", 30));

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20,
						  "observacao": "Entrada de compra de fornecedor"
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idMovimentacao").value(1))
				.andExpect(jsonPath("$.produtoId").value(10))
				.andExpect(jsonPath("$.produtoNome").value("Teclado mecanico"))
				.andExpect(jsonPath("$.quantidadeAtual").value(30));
	}

	@Test
	void deveRegistrarEntradaQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(registrarEntradaEstoqueService.registrar(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new RegistrarEntradaResponse(2, 8, "Mouse gamer", 15));

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 8,
						  "quantidade": 10,
						  "observacao": null
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idMovimentacao").value(2))
				.andExpect(jsonPath("$.produtoNome").value("Mouse gamer"))
				.andExpect(jsonPath("$.quantidadeAtual").value(15));
	}

	@Test
	void deveRetornarUnauthorizedNoPostQuandoTokenNaoForInformado() throws Exception {
		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedNoPostQuandoTokenForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarForbiddenNoPostQuandoUsuarioNaoTiverPermissao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenNoPostQuandoUsuarioAutenticadoEstiverInativoOuInexistente() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(registrarEntradaEstoqueService.registrar(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistirNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(registrarEntradaEstoqueService.registrar(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new ProductNotFoundException());

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 999,
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Produto nao encontrado."));
	}

	@Test
	void deveRetornarForbiddenQuandoProdutoEstiverInativoNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(registrarEntradaEstoqueService.registrar(org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new InactiveProductMovementNotAllowedException());

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Produto inativo nao pode receber movimentacao."));
	}

	@Test
	void deveRetornarBadRequestQuandoProdutoIdNaoForInformadoNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "quantidade": 20
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("O produto e obrigatorio."));
	}

	@Test
	void deveRetornarBadRequestQuandoQuantidadeNaoForInformadaNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("A quantidade e obrigatoria."));
	}

	@Test
	void deveRetornarBadRequestQuandoQuantidadeForZeroNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 0
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("A quantidade deve ser maior que zero."));
	}

	@Test
	void deveRetornarBadRequestQuandoQuantidadeForNegativaNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": -1
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("A quantidade deve ser maior que zero."));
	}

	@Test
	void deveRetornarBadRequestQuandoObservacaoUltrapassarLimiteNoRegistroDeEntrada() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);

		mockMvc.perform(post("/v1/api/movimentacoes/entrada")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "produtoId": 10,
						  "quantidade": 20,
						  "observacao": "%s"
						}
						""".formatted("x".repeat(256)))
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("A observacao deve ter no maximo 255 caracteres."));
	}
}
