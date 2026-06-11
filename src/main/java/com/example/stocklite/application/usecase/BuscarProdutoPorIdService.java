package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProductNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class BuscarProdutoPorIdService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BuscarProdutoPorIdService.class);

	private final ProdutoRepository produtoRepository;
	private final UsuarioRepository usuarioRepository;

	public BuscarProdutoPorIdService(
			ProdutoRepository produtoRepository,
			UsuarioRepository usuarioRepository) {
		this.produtoRepository = produtoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public ProdutoDetalheResponse buscar(Integer idProduto, AuthenticatedUser usuarioAutenticado) {
		validarUsuarioAutenticado(usuarioAutenticado);

		Produto produto = produtoRepository.findById(idProduto)
				.orElseThrow(() -> {
					LOGGER.warn("Produto nao encontrado. idProduto={}", idProduto);
					return new ProductNotFoundException();
				});

		return toResponse(produto);
	}

	private void validarUsuarioAutenticado(AuthenticatedUser usuarioAutenticado) {
		Usuario usuario = usuarioRepository.findById(usuarioAutenticado.idUsuario())
				.orElseThrow(() -> {
					LOGGER.warn("Usuario autenticado nao encontrado. idUsuario={}", usuarioAutenticado.idUsuario());
					return new AuthenticatedUserInactiveOrNotFoundException();
				});

		if (usuario.estaInativo()) {
			LOGGER.warn("Usuario autenticado inativo tentando consultar produto. idUsuario={}", usuario.getIdUsuario());
			throw new AuthenticatedUserInactiveOrNotFoundException();
		}
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
