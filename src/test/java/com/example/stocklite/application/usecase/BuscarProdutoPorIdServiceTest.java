package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class BuscarProdutoPorIdServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private BuscarProdutoPorIdService buscarProdutoPorIdService;

	private Produto produto;

	@BeforeEach
	void setUp() {
		Perfil perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

		produto = Produto.builder()
				.idProduto(10)
				.nome("Mouse sem fio")
				.descricao("Mouse sem fio USB")
				.preco(new BigDecimal("59.90"))
				.quantidadeEstoque(25)
				.ativo(Boolean.TRUE)
				.build();
	}

	@Test
	void deveRetornarProdutoQuandoUsuarioAutenticadoForValidoEProdutoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));

		ProdutoDetalheResponse response = buscarProdutoPorIdService.buscar(10, usuarioAutenticado);

		assertEquals(new ProdutoDetalheResponse(
				10,
				"Mouse sem fio",
				"Mouse sem fio USB",
				25,
				new BigDecimal("59.90"),
				Boolean.TRUE), response);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "tentando consultar produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> buscarProdutoPorIdService.buscar(10, usuarioAutenticado));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "inativo@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "tentando consultar produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> buscarProdutoPorIdService.buscar(10, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(produtoRepository.findById(10)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class,
				() -> buscarProdutoPorIdService.buscar(10, usuarioAutenticado));
	}
}
