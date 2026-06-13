package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.RegistrarEntradaRequest;
import com.example.stocklite.application.dto.RegistrarEntradaResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.InactiveProductMovementNotAllowedException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.MovimentacaoRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class RegistrarEntradaEstoqueServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private MovimentacaoRepository movimentacaoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private RegistrarEntradaEstoqueService registrarEntradaEstoqueService;

	@Test
	void deveRegistrarEntradaAtualizandoEstoqueEPersistindoMovimentacao() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarEntradaRequest request = new RegistrarEntradaRequest(10, 20, "  Entrada de compra de fornecedor  ");
		Usuario usuario = usuario(1, "Administrador");
		Produto produto = produto(10, "Teclado mecanico", 10, true);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));
		when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
			MovimentacaoEstoque movimentacao = invocation.getArgument(0, MovimentacaoEstoque.class);
			movimentacao.setIdMovimentacao(1);
			return movimentacao;
		});

		RegistrarEntradaResponse response = registrarEntradaEstoqueService.registrar(request, usuarioAutenticado);

		ArgumentCaptor<MovimentacaoEstoque> movimentacaoCaptor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque");
		verify(movimentacaoRepository).save(movimentacaoCaptor.capture());

		MovimentacaoEstoque movimentacaoSalva = movimentacaoCaptor.getValue();
		assertEquals("ENTRADA", movimentacaoSalva.getTipo());
		assertEquals(20, movimentacaoSalva.getQuantidade());
		assertEquals("Entrada de compra de fornecedor", movimentacaoSalva.getObservacao());
		assertEquals(30, produto.getQuantidadeEstoque());
		assertEquals(new RegistrarEntradaResponse(1, 10, "Teclado mecanico", 30), response);
	}

	@Test
	void deveRegistrarEntradaComObservacaoNulaQuandoVierEmBranco() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");
		RegistrarEntradaRequest request = new RegistrarEntradaRequest(8, 5, "   ");
		Usuario usuario = usuario(2, "Maria Souza");
		Produto produto = produto(8, "Mouse gamer", 10, true);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(8)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));
		when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
			MovimentacaoEstoque movimentacao = invocation.getArgument(0, MovimentacaoEstoque.class);
			movimentacao.setIdMovimentacao(2);
			return movimentacao;
		});

		RegistrarEntradaResponse response = registrarEntradaEstoqueService.registrar(request, usuarioAutenticado);

		ArgumentCaptor<MovimentacaoEstoque> movimentacaoCaptor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(movimentacaoRepository).save(movimentacaoCaptor.capture());
		assertNull(movimentacaoCaptor.getValue().getObservacao());
		assertEquals(15, response.quantidadeAtual());
	}

	@Test
	void deveFalharQuandoProdutoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarEntradaRequest request = new RegistrarEntradaRequest(999, 20, "Entrada");
		Usuario usuario = usuario(1, "Administrador");

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class,
				() -> registrarEntradaEstoqueService.registrar(request, usuarioAutenticado));
	}

	@Test
	void deveFalharQuandoProdutoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarEntradaRequest request = new RegistrarEntradaRequest(10, 20, "Entrada");
		Usuario usuario = usuario(1, "Administrador");
		Produto produto = produto(10, "Teclado mecanico", 10, false);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));

		assertThrows(InactiveProductMovementNotAllowedException.class,
				() -> registrarEntradaEstoqueService.registrar(request, usuarioAutenticado));
	}

	@Test
	void devePropagarFalhaQuandoUsuarioAutenticadoForInvalido() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");
		RegistrarEntradaRequest request = new RegistrarEntradaRequest(10, 20, "Entrada");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao registrar entrada de estoque");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> registrarEntradaEstoqueService.registrar(request, usuarioAutenticado));
	}

	private Produto produto(Integer idProduto, String nome, Integer quantidadeEstoque, boolean ativo) {
		Produto produto = new Produto();
		produto.setIdProduto(idProduto);
		produto.setNome(nome);
		produto.setQuantidadeEstoque(quantidadeEstoque);
		produto.setAtivo(ativo);
		return produto;
	}

	private Usuario usuario(Integer idUsuario, String nome) {
		Perfil perfil = new Perfil();
		perfil.setIdPerfil(1);
		perfil.setNome("ADMIN");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(idUsuario);
		usuario.setNome(nome);
		usuario.setPerfil(perfil);
		usuario.setAtivo(Boolean.TRUE);
		return usuario;
	}
}
