package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.CadastrarProdutoRequest;
import com.example.stocklite.application.dto.CadastrarProdutoResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class CadastrarProdutoServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private CadastrarProdutoService cadastrarProdutoService;

	@BeforeEach
	void setUp() {
		Perfil perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

	}

	@Test
	void deveCadastrarProdutoQuandoUsuarioAutenticadoForValido() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		CadastrarProdutoRequest request = new CadastrarProdutoRequest(
				"  Teclado mecanico  ",
				"  Teclado ABNT2  ",
				new BigDecimal("250.00"),
				10);

		when(produtoRepository.existsByNomeIgnoreCase("Teclado mecanico")).thenReturn(false);
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
			Produto produto = invocation.getArgument(0, Produto.class);
			produto.setIdProduto(1);
			return produto;
		});

		CadastrarProdutoResponse response = cadastrarProdutoService.cadastrar(request, usuarioAutenticado);

		assertEquals(new CadastrarProdutoResponse(
				1,
				"Teclado mecanico",
				"Teclado ABNT2",
				new BigDecimal("250.00"),
				10,
				Boolean.TRUE), response);
	}

	@Test
	void deveCadastrarProdutoComDescricaoNulaQuandoDescricaoVierVazia() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		CadastrarProdutoRequest request = new CadastrarProdutoRequest(
				"Mouse gamer",
				"   ",
				new BigDecimal("150.00"),
				5);

		when(produtoRepository.existsByNomeIgnoreCase("Mouse gamer")).thenReturn(false);
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
			Produto produto = invocation.getArgument(0, Produto.class);
			produto.setIdProduto(2);
			return produto;
		});

		CadastrarProdutoResponse response = cadastrarProdutoService.cadastrar(request, usuarioAutenticado);

		assertNull(response.descricao());
		assertEquals(5, response.quantidadeAtual());
		assertEquals(Boolean.TRUE, response.status());
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");
		CadastrarProdutoRequest request = new CadastrarProdutoRequest(
				"Teclado mecanico",
				"Teclado ABNT2",
				new BigDecimal("250.00"),
				10);

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao cadastrar produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> cadastrarProdutoService.cadastrar(request, usuarioAutenticado));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "inativo@email.com", "ADMIN");
		CadastrarProdutoRequest request = new CadastrarProdutoRequest(
				"Teclado mecanico",
				"Teclado ABNT2",
				new BigDecimal("250.00"),
				10);

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao cadastrar produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> cadastrarProdutoService.cadastrar(request, usuarioAutenticado));
	}

	@Test
	void deveRetornarConflictQuandoProdutoJaExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		CadastrarProdutoRequest request = new CadastrarProdutoRequest(
				"Teclado mecanico",
				"Teclado ABNT2",
				new BigDecimal("250.00"),
				10);

		when(produtoRepository.existsByNomeIgnoreCase("Teclado mecanico")).thenReturn(true);

		assertThrows(ProductAlreadyExistsException.class,
				() -> cadastrarProdutoService.cadastrar(request, usuarioAutenticado));
	}
}
