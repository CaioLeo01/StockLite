package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class InativarProdutoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InativarProdutoService.class);

	private final ProdutoRepository produtoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public InativarProdutoService(
			ProdutoRepository produtoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public MessageResponse inativar(Integer idProduto, AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao excluir produto");

		Produto produto = produtoRepository.findById(idProduto)
				.orElseThrow(() -> {
					LOGGER.warn("Produto nao encontrado para exclusao logica. idProduto={}", idProduto);
					return new ProductNotFoundException();
				});

		if (produto.estaInativo()) {
			return new MessageResponse("Produto ja estava inativo.");
		}

		produto.inativar();
		produtoRepository.save(produto);

		return new MessageResponse("Produto excluido com sucesso.");
	}
}
