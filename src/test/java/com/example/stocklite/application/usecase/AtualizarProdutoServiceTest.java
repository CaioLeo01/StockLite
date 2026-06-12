package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.AtualizarProdutoRequest;
import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class AtualizarProdutoServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private AtualizarProdutoService atualizarProdutoService;

	@Test
	void deveAtualizarProdutoQuandoUsuarioAutenticadoForValido() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"  Teclado mecanico atualizado  ",
				"  Teclado ABNT2  ",
				new BigDecimal("275.00"),
				15);
		Produto produto = produtoCom(1, "Teclado mecanico", "Anterior", new BigDecimal("250.00"), 10);

		when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
		when(produtoRepository.findByNomeIgnoreCase("Teclado mecanico atualizado")).thenReturn(Optional.empty());
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));

		MessageResponse response = atualizarProdutoService.atualizar(1, request, usuarioAutenticado);

		assertEquals(new MessageResponse("Produto atualizado com sucesso"), response);
		assertEquals("Teclado mecanico atualizado", produto.getNome());
		assertEquals("Teclado ABNT2", produto.getDescricao());
		assertEquals(new BigDecimal("275.00"), produto.getPreco());
		assertEquals(15, produto.getQuantidadeEstoque());
	}

	@Test
	void deveAtualizarProdutoComDescricaoNulaQuandoDescricaoVierVazia() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"Mouse gamer",
				"   ",
				new BigDecimal("150.00"),
				5);
		Produto produto = produtoCom(2, "Mouse antigo", "Descricao", new BigDecimal("140.00"), 3);

		when(produtoRepository.findById(2)).thenReturn(Optional.of(produto));
		when(produtoRepository.findByNomeIgnoreCase("Mouse gamer")).thenReturn(Optional.empty());
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));

		atualizarProdutoService.atualizar(2, request, usuarioAutenticado);

		assertNull(produto.getDescricao());
		assertEquals(5, produto.getQuantidadeEstoque());
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistirOuEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"Produto",
				"Descricao",
				new BigDecimal("10.00"),
				1);

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao atualizar produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> atualizarProdutoService.atualizar(1, request, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"Produto",
				"Descricao",
				new BigDecimal("10.00"),
				1);

		when(produtoRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class,
				() -> atualizarProdutoService.atualizar(99, request, usuarioAutenticado));
	}

	@Test
	void deveRetornarConflictQuandoOutroProdutoJaUtilizarONomeInformado() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"Teclado mecanico",
				"Descricao",
				new BigDecimal("250.00"),
				10);
		Produto produtoAlvo = produtoCom(1, "Produto atual", "Descricao", new BigDecimal("200.00"), 8);
		Produto produtoConflitante = produtoCom(2, "Teclado mecanico", "Descricao", new BigDecimal("250.00"), 10);

		when(produtoRepository.findById(1)).thenReturn(Optional.of(produtoAlvo));
		when(produtoRepository.findByNomeIgnoreCase("Teclado mecanico")).thenReturn(Optional.of(produtoConflitante));

		assertThrows(ProductAlreadyExistsException.class,
				() -> atualizarProdutoService.atualizar(1, request, usuarioAutenticado));
		verify(produtoRepository, never()).save(any(Produto.class));
	}

	@Test
	void devePermitirAtualizacaoQuandoNomePertencerAoProprioProduto() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarProdutoRequest request = new AtualizarProdutoRequest(
				"Teclado mecanico",
				"Descricao nova",
				new BigDecimal("255.00"),
				11);
		Produto produto = produtoCom(1, "Teclado mecanico", "Descricao antiga", new BigDecimal("200.00"), 8);

		when(produtoRepository.findById(1)).thenReturn(Optional.of(produto));
		when(produtoRepository.findByNomeIgnoreCase("Teclado mecanico")).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));

		MessageResponse response = atualizarProdutoService.atualizar(1, request, usuarioAutenticado);

		assertEquals("Produto atualizado com sucesso", response.mensagem());
		assertEquals("Descricao nova", produto.getDescricao());
		assertEquals(new BigDecimal("255.00"), produto.getPreco());
		assertEquals(11, produto.getQuantidadeEstoque());
	}

	private Produto produtoCom(
			Integer idProduto,
			String nome,
			String descricao,
			BigDecimal preco,
			Integer quantidadeEstoque) {
		Produto produto = new Produto();
		produto.setIdProduto(idProduto);
		produto.setNome(nome);
		produto.setDescricao(descricao);
		produto.setPreco(preco);
		produto.setQuantidadeEstoque(quantidadeEstoque);
		produto.setAtivo(Boolean.TRUE);
		return produto;
	}
}
