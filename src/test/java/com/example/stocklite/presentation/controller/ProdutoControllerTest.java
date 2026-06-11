package com.example.stocklite.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.BuscarProdutoPorIdService;
import com.example.stocklite.application.usecase.ListarProdutosService;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataPerfilRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataProdutoRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataUsuarioRepository;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
class ProdutoControllerTest {

	private static final String CONTEXT_PATH = "/v1/api";
	private static final String TOKEN_VALIDO = "jwt-valido";
	private static final String TOKEN_INVALIDO = "jwt-invalido";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BuscarProdutoPorIdService buscarProdutoPorIdService;

	@MockitoBean
	private ListarProdutosService listarProdutosService;

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private ProdutoRepository produtoRepository;

	@MockitoBean
	private PerfilRepository perfilRepository;

	@MockitoBean
	private SpringDataUsuarioRepository springDataUsuarioRepository;

	@MockitoBean
	private SpringDataPerfilRepository springDataPerfilRepository;

	@MockitoBean
	private SpringDataProdutoRepository springDataProdutoRepository;

	@MockitoBean
	private PasswordHasher passwordHasher;

	@Test
	void deveListarProdutosQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarProdutosService.listar(usuarioAutenticado)).thenReturn(List.of(
				new ProdutoListagemResponse(1, "Mouse sem fio", new BigDecimal("59.90")),
				new ProdutoListagemResponse(2, "Teclado mecanico", new BigDecimal("199.90"))));

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].idProduto").value(1))
				.andExpect(jsonPath("$[0].nome").value("Mouse sem fio"))
				.andExpect(jsonPath("$[0].preco").value(59.90))
				.andExpect(jsonPath("$[1].idProduto").value(2))
				.andExpect(jsonPath("$[1].nome").value("Teclado mecanico"))
				.andExpect(jsonPath("$[1].preco").value(199.90));
	}

	@Test
	void deveListarProdutosQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarProdutosService.listar(usuarioAutenticado)).thenReturn(List.of(
				new ProdutoListagemResponse(3, "Monitor", new BigDecimal("899.90"))));

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nome").value("Monitor"));
	}

	@Test
	void deveListarProdutosQuandoTokenForValidoEUsuarioForVisualizador() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarProdutosService.listar(usuarioAutenticado)).thenReturn(List.of(
				new ProdutoListagemResponse(4, "Notebook", new BigDecimal("3999.90"))));

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nome").value("Notebook"))
				.andExpect(jsonPath("$[0].preco").value(3999.90));
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverProdutos() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarProdutosService.listar(usuarioAutenticado)).thenReturn(List.of());

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void deveBuscarProdutoQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(buscarProdutoPorIdService.buscar(1, usuarioAutenticado)).thenReturn(
				new ProdutoDetalheResponse(1, "Mouse sem fio", "Mouse sem fio USB", 25, new BigDecimal("59.90"), Boolean.TRUE));

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.idProduto").value(1))
				.andExpect(jsonPath("$.nome").value("Mouse sem fio"))
				.andExpect(jsonPath("$.descricao").value("Mouse sem fio USB"))
				.andExpect(jsonPath("$.quantidadeEstoque").value(25))
				.andExpect(jsonPath("$.preco").value(59.90))
				.andExpect(jsonPath("$.status").value(true));
	}

	@Test
	void deveBuscarProdutoQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(buscarProdutoPorIdService.buscar(1, usuarioAutenticado)).thenReturn(
				new ProdutoDetalheResponse(1, "Teclado", "Teclado USB", 12, new BigDecimal("99.90"), Boolean.TRUE));

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Teclado"));
	}

	@Test
	void deveBuscarProdutoQuandoTokenForValidoEUsuarioForVisualizador() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(buscarProdutoPorIdService.buscar(1, usuarioAutenticado)).thenReturn(
				new ProdutoDetalheResponse(1, "Monitor", "Monitor 24", 8, new BigDecimal("899.90"), Boolean.FALSE));

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Monitor"))
				.andExpect(jsonPath("$.status").value(false));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformado() throws Exception {
		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformadoNaBuscaPorId() throws Exception {
		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalidoNaBuscaPorId() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissaoNaBuscaPorId() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistente() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listarProdutosService.listar(usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(get("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistenteNaBuscaPorId() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "VISUALIZADOR");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(buscarProdutoPorIdService.buscar(1, usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(get("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistir() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(buscarProdutoPorIdService.buscar(99, usuarioAutenticado))
				.thenThrow(new ProductNotFoundException());

		mockMvc.perform(get("/v1/api/produtos/99")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Produto nao encontrado."));
	}

	@Test
	void deveRetornarBadRequestQuandoIdNaoForNumerico() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(get("/v1/api/produtos/abc")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Identificador do produto invalido."));
	}
}
