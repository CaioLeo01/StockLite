package com.example.stocklite.application.usecase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class ListarProdutosService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListarProdutosService.class);

	private final ProdutoRepository produtoRepository;
	private final UsuarioRepository usuarioRepository;

	public ListarProdutosService(
			ProdutoRepository produtoRepository,
			UsuarioRepository usuarioRepository) {
		this.produtoRepository = produtoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	public List<ProdutoListagemResponse> listar(AuthenticatedUser usuarioAutenticado) {
		validarUsuarioAutenticado(usuarioAutenticado);

		return produtoRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	private void validarUsuarioAutenticado(AuthenticatedUser usuarioAutenticado) {
		Usuario usuario = usuarioRepository.findById(usuarioAutenticado.idUsuario())
				.orElseThrow(() -> {
					LOGGER.warn("Usuario autenticado nao encontrado ao listar produtos. idUsuario={}",
							usuarioAutenticado.idUsuario());
					return new AuthenticatedUserInactiveOrNotFoundException();
				});

		if (usuario.estaInativo()) {
			LOGGER.warn("Usuario autenticado inativo tentando listar produtos. idUsuario={}", usuario.getIdUsuario());
			throw new AuthenticatedUserInactiveOrNotFoundException();
		}
	}

	private ProdutoListagemResponse toResponse(Produto produto) {
		return new ProdutoListagemResponse(
				produto.getIdProduto(),
				produto.getNome(),
				produto.getPreco());
	}
}
