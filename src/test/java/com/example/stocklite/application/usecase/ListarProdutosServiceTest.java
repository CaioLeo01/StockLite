package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ListarProdutosServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private ListarProdutosService listarProdutosService;

	private Produto produtoMouse;
	private Produto produtoTeclado;

	@BeforeEach
	void setUp() {
		Perfil perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

		produtoMouse = Produto.builder()
				.idProduto(10)
				.nome("Mouse sem fio")
				.descricao("Mouse sem fio USB")
				.preco(new BigDecimal("59.90"))
				.quantidadeEstoque(25)
				.ativo(Boolean.TRUE)
				.build();

		produtoTeclado = Produto.builder()
				.idProduto(11)
				.nome("Teclado mecanico")
				.descricao("Teclado mecanico USB")
				.preco(new BigDecimal("199.90"))
				.quantidadeEstoque(12)
				.ativo(Boolean.FALSE)
				.build();
	}

	@Test
	void deveRetornarApenasProdutosAtivosQuandoUsuarioAutenticadoForValido() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(produtoRepository.findAll()).thenReturn(List.of(produtoMouse, produtoTeclado));

		List<ProdutoListagemResponse> response = listarProdutosService.listar(usuarioAutenticado);

		assertEquals(List.of(
				new ProdutoListagemResponse(10, "Mouse sem fio", new BigDecimal("59.90"))), response);
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverProdutos() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(produtoRepository.findAll()).thenReturn(List.of());

		List<ProdutoListagemResponse> response = listarProdutosService.listar(usuarioAutenticado);

		assertEquals(List.of(), response);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao listar produtos");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listarProdutosService.listar(usuarioAutenticado));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "inativo@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao listar produtos");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listarProdutosService.listar(usuarioAutenticado));
	}
}
