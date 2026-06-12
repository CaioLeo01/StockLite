package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.CadastrarProdutoRequest;
import com.example.stocklite.application.dto.CadastrarProdutoResponse;
import com.example.stocklite.application.exception.ProductAlreadyExistsException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class CadastrarProdutoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CadastrarProdutoService.class);

	private final ProdutoRepository produtoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public CadastrarProdutoService(
			ProdutoRepository produtoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public CadastrarProdutoResponse cadastrar(
			CadastrarProdutoRequest request,
			AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao cadastrar produto");

		String nomeNormalizado = normalizarNome(request.nome());

		if (produtoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
			LOGGER.warn("Produto duplicado no cadastro. nome={}", nomeNormalizado);
			throw new ProductAlreadyExistsException();
		}

		Produto produto = Produto.builder()
				.nome(nomeNormalizado)
				.descricao(normalizarDescricao(request.descricao()))
				.preco(request.preco())
				.quantidadeEstoque(request.quantidadeInicial())
				.ativo(Boolean.TRUE)
				.build();

		Produto produtoSalvo = produtoRepository.save(produto);

		return new CadastrarProdutoResponse(
				produtoSalvo.getIdProduto(),
				produtoSalvo.getNome(),
				produtoSalvo.getDescricao(),
				produtoSalvo.getPreco(),
				produtoSalvo.getQuantidadeEstoque(),
				produtoSalvo.getAtivo());
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
