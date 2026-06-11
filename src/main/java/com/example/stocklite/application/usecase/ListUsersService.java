package com.example.stocklite.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.UsuarioListagemResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class ListUsersService {

	private final UsuarioRepository usuarioRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public ListUsersService(
			UsuarioRepository usuarioRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.usuarioRepository = usuarioRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public List<UsuarioListagemResponse> listar(AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, null);

		return usuarioRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
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
