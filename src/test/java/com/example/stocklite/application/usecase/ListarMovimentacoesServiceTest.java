package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.MovimentacaoResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.MovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
class ListarMovimentacoesServiceTest {

	@Mock
	private MovimentacaoRepository movimentacaoRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private ListarMovimentacoesService listarMovimentacoesService;

	@Test
	void deveListarMovimentacoesMapeandoDadosEPreservandoOrdem() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		OffsetDateTime dataMaisRecente = OffsetDateTime.parse("2026-06-12T11:00:00Z");
		OffsetDateTime dataMaisAntiga = OffsetDateTime.parse("2026-06-12T10:30:00Z");

		MovimentacaoEstoque movimentacaoSaida = MovimentacaoEstoque.builder()
				.idMovimentacao(2)
				.produto(produto(10, "Teclado mecanico"))
				.usuario(usuario(2, "Maria Souza"))
				.tipo("SAIDA")
				.quantidade(5)
				.dataMovimentacao(dataMaisRecente)
				.observacao("Retirada para uso interno")
				.build();

		MovimentacaoEstoque movimentacaoEntrada = MovimentacaoEstoque.builder()
				.idMovimentacao(1)
				.produto(produto(10, "Teclado mecanico"))
				.usuario(usuario(1, "Joao da Silva"))
				.tipo("ENTRADA")
				.quantidade(20)
				.dataMovimentacao(dataMaisAntiga)
				.observacao("Entrada inicial de estoque")
				.build();

		when(movimentacaoRepository.findAllOrderByDataMovimentacaoDesc())
				.thenReturn(List.of(movimentacaoSaida, movimentacaoEntrada));

		List<MovimentacaoResponse> response = listarMovimentacoesService.listar(usuarioAutenticado);

		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, "ao listar movimentacoes");
		assertEquals(List.of(
				new MovimentacaoResponse(2, 10, "Teclado mecanico", 2, "Maria Souza", "SAIDA", 5,
						dataMaisRecente, "Retirada para uso interno"),
				new MovimentacaoResponse(1, 10, "Teclado mecanico", 1, "Joao da Silva", "ENTRADA", 20,
						dataMaisAntiga, "Entrada inicial de estoque")), response);
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverMovimentacoes() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(movimentacaoRepository.findAllOrderByDataMovimentacaoDesc()).thenReturn(List.of());

		List<MovimentacaoResponse> response = listarMovimentacoesService.listar(usuarioAutenticado);

		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, "ao listar movimentacoes");
		assertEquals(List.of(), response);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistirOuEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, "ao listar movimentacoes");

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listarMovimentacoesService.listar(usuarioAutenticado));
	}

	private Produto produto(Integer idProduto, String nome) {
		Produto produto = new Produto();
		produto.setIdProduto(idProduto);
		produto.setNome(nome);
		return produto;
	}

	private Usuario usuario(Integer idUsuario, String nome) {
		Usuario usuario = new Usuario();
		usuario.setIdUsuario(idUsuario);
		usuario.setNome(nome);
		return usuario;
	}
}
