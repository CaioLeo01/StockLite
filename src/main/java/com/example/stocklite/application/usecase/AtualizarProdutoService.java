package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.AtualizarProdutoRequest;
import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class AtualizarProdutoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AtualizarProdutoService.class);

	private final ProdutoRepository produtoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public AtualizarProdutoService(
			ProdutoRepository produtoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public MessageResponse atualizar(
			Integer idProduto,
			AtualizarProdutoRequest request,
			AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao atualizar produto");

		Produto produto = produtoRepository.findById(idProduto)
				.orElseThrow(() -> {
					LOGGER.warn("Produto nao encontrado para atualizacao. idProduto={}", idProduto);
					return new ProductNotFoundException();
				});

		String nomeNormalizado = normalizarNome(request.nome());
		validarConflitoNome(idProduto, nomeNormalizado);

		produto.setNome(nomeNormalizado);
		produto.setDescricao(normalizarDescricao(request.descricao()));
		produto.setPreco(request.preco());
		produto.setQuantidadeEstoque(request.quantidadeEstoque());

		produtoRepository.save(produto);

		return new MessageResponse("Produto atualizado com sucesso");
	}

	private void validarConflitoNome(Integer idProduto, String nomeNormalizado) {
		produtoRepository.findByNomeIgnoreCase(nomeNormalizado)
				.filter(produto -> !idProduto.equals(produto.getIdProduto()))
				.ifPresent(produto -> {
					LOGGER.warn("Conflito de nome na atualizacao do produto. idProduto={}, nome={}",
							idProduto,
							nomeNormalizado);
					throw new ProductAlreadyExistsException();
				});
	}

	private String normalizarNome(String nome) {
		return nome.trim();
	}

	private String normalizarDescricao(String descricao) {
		if (descricao == null) {
			return null;
		}

		String descricaoNormalizada = descricao.trim();
		if (descricaoNormalizada.isEmpty()) {
			return null;
		}

		return descricaoNormalizada;
	}
}
