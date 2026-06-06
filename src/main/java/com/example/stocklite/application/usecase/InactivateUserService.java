package com.example.stocklite.application.usecase;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class InactivateUserService {

	private final UsuarioRepository usuarioRepository;

	public InactivateUserService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public InactivateUserResult inativar(Integer idUsuarioAlvo, AuthenticatedUser usuarioAutenticado) {
		validarUsuarioAutenticado(usuarioAutenticado);
		validarAutoInativacao(idUsuarioAlvo, usuarioAutenticado);

		Usuario usuario = usuarioRepository.findById(idUsuarioAlvo)
				.orElseThrow(UserNotFoundException::new);

		if (usuario.estaInativo()) {
			return InactivateUserResult.JA_ESTAVA_INATIVO;
		}

		usuario.inativar();
		usuarioRepository.save(usuario);

		return InactivateUserResult.INATIVADO;
	}

	private void validarAutoInativacao(Integer idUsuarioAlvo, AuthenticatedUser usuarioAutenticado) {
		if (idUsuarioAlvo.equals(usuarioAutenticado.idUsuario())) {
			throw new SelfUserDeletionNotAllowedException();
		}
	}

	private void validarUsuarioAutenticado(AuthenticatedUser usuarioAutenticado) {
		Usuario usuario = usuarioRepository.findById(usuarioAutenticado.idUsuario())
				.orElseThrow(AuthenticatedUserInactiveOrNotFoundException::new);

		if (usuario.estaInativo()) {
			throw new AuthenticatedUserInactiveOrNotFoundException();
		}
	}
}
