package com.example.stocklite.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.dto.CadastrarProdutoResponse;
import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.AtualizarProdutoService;
import com.example.stocklite.application.usecase.BuscarProdutoPorIdService;
import com.example.stocklite.application.usecase.CadastrarProdutoService;
import com.example.stocklite.application.usecase.InativarProdutoService;
import com.example.stocklite.application.usecase.ListarMovimentacoesService;
import com.example.stocklite.application.usecase.ListarProdutosService;
import com.example.stocklite.application.usecase.RegistrarEntradaEstoqueService;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataPerfilRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataMovimentacaoRepository;
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
	private AtualizarProdutoService atualizarProdutoService;

	@MockitoBean
	private CadastrarProdutoService cadastrarProdutoService;

	@MockitoBean
	private InativarProdutoService inativarProdutoService;

	@MockitoBean
	private ListarProdutosService listarProdutosService;

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
	void deveCadastrarProdutoQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(cadastrarProdutoService.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new CadastrarProdutoResponse(
						1,
						"Teclado mecanico",
						"Teclado ABNT2",
						new BigDecimal("250.00"),
						10,
						Boolean.TRUE));

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idProduto").value(1))
				.andExpect(jsonPath("$.nome").value("Teclado mecanico"))
				.andExpect(jsonPath("$.descricao").value("Teclado ABNT2"))
				.andExpect(jsonPath("$.preco").value(250.00))
				.andExpect(jsonPath("$.quantidadeAtual").value(10))
				.andExpect(jsonPath("$.status").value(true));
	}

	@Test
	void deveCadastrarProdutoQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(cadastrarProdutoService.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new CadastrarProdutoResponse(
						2,
						"Mouse gamer",
						null,
						new BigDecimal("150.00"),
						5,
						Boolean.TRUE));

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Mouse gamer",
						  "descricao": null,
						  "preco": 150.00,
						  "quantidadeInicial": 5
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idProduto").value(2))
				.andExpect(jsonPath("$.nome").value("Mouse gamer"))
				.andExpect(jsonPath("$.descricao").value(Matchers.nullValue()))
				.andExpect(jsonPath("$.preco").value(150.00))
				.andExpect(jsonPath("$.quantidadeAtual").value(5))
				.andExpect(jsonPath("$.status").value(true));
	}

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
	void deveAtualizarProdutoQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(atualizarProdutoService.atualizar(
				org.mockito.ArgumentMatchers.eq(1),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new com.example.stocklite.application.dto.MessageResponse("Produto atualizado com sucesso"));

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Produto atualizado com sucesso"));
	}

	@Test
	void deveAtualizarProdutoQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(atualizarProdutoService.atualizar(
				org.mockito.ArgumentMatchers.eq(1),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(new com.example.stocklite.application.dto.MessageResponse("Produto atualizado com sucesso"));

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Mouse gamer atualizado",
						  "descricao": null,
						  "preco": 155.00,
						  "quantidadeEstoque": 6
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Produto atualizado com sucesso"));
	}

	@Test
	void deveExcluirProdutoQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inativarProdutoService.inativar(1, usuarioAutenticado))
				.thenReturn(new com.example.stocklite.application.dto.MessageResponse("Produto excluido com sucesso."));

		mockMvc.perform(delete("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Produto excluido com sucesso."));
	}

	@Test
	void deveExcluirProdutoQuandoTokenForValidoEUsuarioForEstoquista() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inativarProdutoService.inativar(1, usuarioAutenticado))
				.thenReturn(new com.example.stocklite.application.dto.MessageResponse("Produto excluido com sucesso."));

		mockMvc.perform(delete("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Produto excluido com sucesso."));
	}

	@Test
	void deveRetornarMensagemIdempotenteQuandoProdutoJaEstiverInativoNaExclusao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inativarProdutoService.inativar(1, usuarioAutenticado))
				.thenReturn(new com.example.stocklite.application.dto.MessageResponse("Produto ja estava inativo."));

		mockMvc.perform(delete("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Produto ja estava inativo."));
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
	void deveRetornarUnauthorizedQuandoTokenNaoForInformadoNoCadastro() throws Exception {
		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformadoNaAtualizacao() throws Exception {
		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformadoNaExclusao() throws Exception {
		mockMvc.perform(delete("/v1/api/produtos/1")
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
	void deveRetornarUnauthorizedQuandoTokenForInvalidoNoCadastro() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalidoNaAtualizacao() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalidoNaExclusao() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(delete("/v1/api/produtos/1")
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
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissaoNoCadastro() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissaoNaAtualizacao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissaoNaExclusao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "operador@email.com", "OPERADOR"));

		mockMvc.perform(delete("/v1/api/produtos/1")
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
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistenteNoCadastro() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(cadastrarProdutoService.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistenteNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(atualizarProdutoService.atualizar(
				org.mockito.ArgumentMatchers.eq(1),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativoOuInexistenteNaExclusao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "viewer@email.com", "ESTOQUISTA");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inativarProdutoService.inativar(1, usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(delete("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosDoCadastroForemInvalidos() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": " ",
						  "descricao": "x".repeat(256),
						  "preco": 0,
						  "quantidadeInicial": -1
						}
						""".replace("\"x\".repeat(256)", "\"" + "x".repeat(256) + "\""))
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value(Matchers.allOf(
						Matchers.containsString("O nome e obrigatorio."),
						Matchers.containsString("A descricao deve ter no maximo 255 caracteres."),
						Matchers.containsString("O preco deve ser maior que zero."),
						Matchers.containsString("A quantidade inicial nao pode ser negativa."))));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosDaAtualizacaoForemInvalidos() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": " ",
						  "descricao": "x".repeat(256),
						  "preco": 0,
						  "quantidadeEstoque": -1
						}
						""".replace("\"x\".repeat(256)", "\"" + "x".repeat(256) + "\""))
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value(Matchers.allOf(
						Matchers.containsString("O nome e obrigatorio."),
						Matchers.containsString("A descricao deve ter no maximo 255 caracteres."),
						Matchers.containsString("O preco deve ser maior que zero."),
						Matchers.containsString("A quantidade em estoque nao pode ser negativa."))));
	}

	@Test
	void deveRetornarConflictQuandoProdutoJaExistirNoCadastro() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(cadastrarProdutoService.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new ProductAlreadyExistsException());

		mockMvc.perform(post("/v1/api/produtos")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico",
						  "descricao": "Teclado ABNT2",
						  "preco": 250.00,
						  "quantidadeInicial": 10
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value("Ja existe um produto cadastrado com estes dados."));
	}

	@Test
	void deveRetornarConflictQuandoProdutoJaExistirNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(atualizarProdutoService.atualizar(
				org.mockito.ArgumentMatchers.eq(1),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new ProductAlreadyExistsException());

		mockMvc.perform(put("/v1/api/produtos/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value("Ja existe um produto cadastrado com estes dados."));
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
	void deveRetornarNotFoundQuandoProdutoNaoExistirNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(atualizarProdutoService.atualizar(
				org.mockito.ArgumentMatchers.eq(99),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new ProductNotFoundException());

		mockMvc.perform(put("/v1/api/produtos/99")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Produto nao encontrado."));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistirNaExclusao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inativarProdutoService.inativar(99, usuarioAutenticado))
				.thenThrow(new ProductNotFoundException());

		mockMvc.perform(delete("/v1/api/produtos/99")
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

	@Test
	void deveRetornarBadRequestQuandoIdDaAtualizacaoNaoForNumerico() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(put("/v1/api/produtos/abc")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Teclado mecanico atualizado",
						  "descricao": "Teclado mecanico ABNT2 com iluminacao RGB",
						  "preco": 275.00,
						  "quantidadeEstoque": 15
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Identificador do produto invalido."));
	}

	@Test
	void deveRetornarBadRequestQuandoIdDaExclusaoNaoForNumerico() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(delete("/v1/api/produtos/abc")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value("Identificador do produto invalido."));
	}
}
