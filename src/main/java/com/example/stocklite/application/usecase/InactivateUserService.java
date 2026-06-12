package com.example.stocklite.application.usecase;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class InactivateUserService {

	private final UsuarioRepository usuarioRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public InactivateUserService(
			UsuarioRepository usuarioRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.usuarioRepository = usuarioRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public InactivateUserResult inativar(Integer idUsuarioAlvo, AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, null);
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
}
