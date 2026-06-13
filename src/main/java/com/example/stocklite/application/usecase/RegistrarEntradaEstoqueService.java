package com.example.stocklite.application.usecase;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stocklite.application.dto.RegistrarEntradaRequest;
import com.example.stocklite.application.dto.RegistrarEntradaResponse;
import com.example.stocklite.application.exception.InactiveProductMovementNotAllowedException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.MovimentacaoRepository;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class RegistrarEntradaEstoqueService {

	private static final String TIPO_ENTRADA = "ENTRADA";

	private final ProdutoRepository produtoRepository;
	private final MovimentacaoRepository movimentacaoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public RegistrarEntradaEstoqueService(
			ProdutoRepository produtoRepository,
			MovimentacaoRepository movimentacaoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.movimentacaoRepository = movimentacaoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	@Transactional
	public RegistrarEntradaResponse registrar(
			RegistrarEntradaRequest request,
			AuthenticatedUser usuarioAutenticado) {
		Usuario usuario = authenticatedUserValidator.validarUsuarioAtivo(
				usuarioAutenticado,
				"ao registrar entrada de estoque");

		Produto produto = produtoRepository.findById(request.produtoId())
				.orElseThrow(ProductNotFoundException::new);

		if (produto.estaInativo()) {
			throw new InactiveProductMovementNotAllowedException();
		}

		produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + request.quantidade());
		Produto produtoAtualizado = produtoRepository.save(produto);

		MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
				.produto(produtoAtualizado)
				.usuario(usuario)
				.tipo(TIPO_ENTRADA)
				.quantidade(request.quantidade())
				.dataMovimentacao(OffsetDateTime.now())
				.observacao(normalizarObservacao(request.observacao()))
				.build();

		MovimentacaoEstoque movimentacaoSalva = movimentacaoRepository.save(movimentacao);

		return new RegistrarEntradaResponse(
				movimentacaoSalva.getIdMovimentacao(),
				produtoAtualizado.getIdProduto(),
				produtoAtualizado.getNome(),
				produtoAtualizado.getQuantidadeEstoque());
	}

	private String normalizarObservacao(String observacao) {
		if (observacao == null) {
			return null;
		}

		String observacaoNormalizada = observacao.trim();
		if (observacaoNormalizada.isEmpty()) {
			return null;
		}

		return observacaoNormalizada;
	}
}
