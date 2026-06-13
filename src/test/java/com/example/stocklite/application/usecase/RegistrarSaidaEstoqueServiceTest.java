package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.RegistrarSaidaRequest;
import com.example.stocklite.application.dto.RegistrarSaidaResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.InactiveProductMovementNotAllowedException;
import com.example.stocklite.application.exception.InsufficientStockException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.MovimentacaoRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class RegistrarSaidaEstoqueServiceTest {

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private MovimentacaoRepository movimentacaoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private RegistrarSaidaEstoqueService registrarSaidaEstoqueService;

	@Test
	void deveRegistrarSaidaAtualizandoEstoqueEPersistindoMovimentacao() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(10, 5, "  Retirada para uso interno  ");
		Usuario usuario = usuario(1, "Administrador");
		Produto produto = produto(10, "Teclado mecanico", 30, true);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));
		when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
			MovimentacaoEstoque movimentacao = invocation.getArgument(0, MovimentacaoEstoque.class);
			movimentacao.setIdMovimentacao(2);
			return movimentacao;
		});

		RegistrarSaidaResponse response = registrarSaidaEstoqueService.registrar(request, usuarioAutenticado);

		ArgumentCaptor<MovimentacaoEstoque> movimentacaoCaptor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque");
		verify(movimentacaoRepository).save(movimentacaoCaptor.capture());

		MovimentacaoEstoque movimentacaoSalva = movimentacaoCaptor.getValue();
		assertEquals("SAIDA", movimentacaoSalva.getTipo());
		assertEquals(5, movimentacaoSalva.getQuantidade());
		assertEquals("Retirada para uso interno", movimentacaoSalva.getObservacao());
		assertEquals(25, produto.getQuantidadeEstoque());
		assertEquals(new RegistrarSaidaResponse(2, 10, "Teclado mecanico", 25), response);
	}

	@Test
	void deveRegistrarSaidaComObservacaoNulaQuandoVierEmBranco() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "estoque@email.com", "ESTOQUISTA");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(8, 3, "   ");
		Usuario usuario = usuario(2, "Maria Souza");
		Produto produto = produto(8, "Mouse gamer", 10, true);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(8)).thenReturn(Optional.of(produto));
		when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0, Produto.class));
		when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
			MovimentacaoEstoque movimentacao = invocation.getArgument(0, MovimentacaoEstoque.class);
			movimentacao.setIdMovimentacao(3);
			return movimentacao;
		});

		RegistrarSaidaResponse response = registrarSaidaEstoqueService.registrar(request, usuarioAutenticado);

		ArgumentCaptor<MovimentacaoEstoque> movimentacaoCaptor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
		verify(movimentacaoRepository).save(movimentacaoCaptor.capture());
		assertNull(movimentacaoCaptor.getValue().getObservacao());
		assertEquals(7, response.quantidadeAtual());
	}

	@Test
	void deveFalharQuandoProdutoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(999, 5, "Saida");
		Usuario usuario = usuario(1, "Administrador");

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(ProductNotFoundException.class,
				() -> registrarSaidaEstoqueService.registrar(request, usuarioAutenticado));
	}

	@Test
	void deveFalharQuandoProdutoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(10, 5, "Saida");
		Usuario usuario = usuario(1, "Administrador");
		Produto produto = produto(10, "Teclado mecanico", 10, false);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));

		assertThrows(InactiveProductMovementNotAllowedException.class,
				() -> registrarSaidaEstoqueService.registrar(request, usuarioAutenticado));
	}

	@Test
	void devePropagarFalhaQuandoUsuarioAutenticadoForInvalido() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(10, 5, "Saida");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> registrarSaidaEstoqueService.registrar(request, usuarioAutenticado));
	}

	@Test
	void deveFalharQuandoQuantidadeSolicitadaExcederEstoqueDisponivel() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		RegistrarSaidaRequest request = new RegistrarSaidaRequest(10, 11, "Saida");
		Usuario usuario = usuario(1, "Administrador");
		Produto produto = produto(10, "Teclado mecanico", 10, true);

		when(authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao registrar saida de estoque"))
				.thenReturn(usuario);
		when(produtoRepository.findById(10)).thenReturn(Optional.of(produto));

		assertThrows(InsufficientStockException.class,
				() -> registrarSaidaEstoqueService.registrar(request, usuarioAutenticado));
		assertEquals(10, produto.getQuantidadeEstoque());
		verify(produtoRepository, never()).save(any(Produto.class));
		verify(movimentacaoRepository, never()).save(any(MovimentacaoEstoque.class));
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
