package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class BuscarProdutoPorIdService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuscarProdutoPorIdService.class);

	private final ProdutoRepository produtoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public BuscarProdutoPorIdService(
			ProdutoRepository produtoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public ProdutoDetalheResponse buscar(Integer idProduto, AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "tentando consultar produto");

		Produto produto = produtoRepository.findById(idProduto)
				.orElseThrow(() -> {
					LOGGER.warn("Produto nao encontrado. idProduto={}", idProduto);
					return new ProductNotFoundException();
				});

		return toResponse(produto);
	}

	private ProdutoDetalheResponse toResponse(Produto produto) {
		return new ProdutoDetalheResponse(
				produto.getIdProduto(),
				produto.getNome(),
				produto.getDescricao(),
				produto.getQuantidadeEstoque(),
				produto.getPreco(),
				produto.getAtivo());
	}
}
