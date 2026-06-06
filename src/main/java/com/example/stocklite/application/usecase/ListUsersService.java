package com.example.stocklite.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.UsuarioListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class ListUsersService {

	private final UsuarioRepository usuarioRepository;

	public ListUsersService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public List<UsuarioListagemResponse> listar(AuthenticatedUser usuarioAutenticado) {
		validarUsuarioAutenticado(usuarioAutenticado);

		return usuarioRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	private void validarUsuarioAutenticado(AuthenticatedUser usuarioAutenticado) {
		Usuario usuario = usuarioRepository.findById(usuarioAutenticado.idUsuario())
				.orElseThrow(AuthenticatedUserInactiveOrNotFoundException::new);

		if (usuario.estaInativo()) {
			throw new AuthenticatedUserInactiveOrNotFoundException();
		}
	}

	private UsuarioListagemResponse toResponse(Usuario usuario) {
		Perfil perfil = usuario.getPerfil();

		return new UsuarioListagemResponse(
				usuario.getIdUsuario(),
				usuario.getNome(),
				usuario.getEmail(),
				perfil != null ? perfil.getIdPerfil() : null,
				perfil != null ? perfil.getNome() : null,
				usuario.getAtivo());
	}
}
