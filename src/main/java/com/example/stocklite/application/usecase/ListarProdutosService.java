package com.example.stocklite.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;

@Service
public class ListarProdutosService {

	private final ProdutoRepository produtoRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public ListarProdutosService(
			ProdutoRepository produtoRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.produtoRepository = produtoRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public List<ProdutoListagemResponse> listar(AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, "ao listar produtos");

		return produtoRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	private ProdutoListagemResponse toResponse(Produto produto) {
		return new ProdutoListagemResponse(
				produto.getIdProduto(),
				produto.getNome(),
				produto.getPreco());
	}
}
