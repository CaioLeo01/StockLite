package com.example.stocklite.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.MovimentacaoResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.repository.MovimentacaoRepository;

@Service
public class ListarMovimentacoesService {

	private final MovimentacaoRepository movimentacaoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public ListarMovimentacoesService(
			MovimentacaoRepository movimentacaoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.movimentacaoRepository = movimentacaoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public List<MovimentacaoResponse> listar(AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao listar movimentacoes");

		return movimentacaoRepository.findAllOrderByDataMovimentacaoDesc().stream()
				.map(this::toResponse)
				.toList();
	}

	private MovimentacaoResponse toResponse(MovimentacaoEstoque movimentacao) {
		return new MovimentacaoResponse(
				movimentacao.getIdMovimentacao(),
				movimentacao.getProduto().getIdProduto(),
				movimentacao.getProduto().getNome(),
				movimentacao.getUsuario().getIdUsuario(),
				movimentacao.getUsuario().getNome(),
				movimentacao.getTipo(),
				movimentacao.getQuantidade(),
				movimentacao.getDataMovimentacao(),
				movimentacao.getObservacao());
	}
}
