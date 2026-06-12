package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class InativarProdutoServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private InativarProdutoService inativarProdutoService;

	@Test
	void deveInativarProdutoQuandoUsuarioAutenticadoForValidoEProdutoEstiverAtivo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		Produto produto = produtoCom(10, Boolean.TRUE);

		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));

		MessageResponse response = inativarProdutoService.inativar(10, usuarioAutenticado);

		assertEquals(new MessageResponse("Produto excluido com sucesso."), response);
		assertFalse(produto.getAtivo());
		verify(produtoRepository).save(produto);
	}

	@Test
	void deveRetornarMensagemIdempotenteQuandoProdutoJaEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		Produto produto = produtoCom(10, Boolean.FALSE);

		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));

		MessageResponse response = inativarProdutoService.inativar(10, usuarioAutenticado);

		assertEquals(new MessageResponse("Produto ja estava inativo."), response);
		verify(produtoRepository, never()).save(any(Produto.class));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistirOuEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao excluir produto");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> inativarProdutoService.inativar(10, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoProdutoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(produtoRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class,
				() -> inativarProdutoService.inativar(99, usuarioAutenticado));
		verify(produtoRepository, never()).save(any(Produto.class));
	}

	private Produto produtoCom(Integer idProduto, Boolean ativo) {
		Produto produto = new Produto();
		produto.setIdProduto(idProduto);
		produto.setNome("Mouse sem fio");
		produto.setDescricao("Mouse sem fio USB");
		produto.setPreco(new BigDecimal("59.90"));
		produto.setQuantidadeEstoque(25);
		produto.setAtivo(ativo);
		return produto;
	}
}
