package com.example.stocklite.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class AuthenticatedUserValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedUserValidator.class);

	private final UsuarioRepository usuarioRepository;

	public AuthenticatedUserValidator(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	public Usuario validarUsuarioAtivo(AuthenticatedUser usuarioAutenticado, String contextoOperacao) {
		Usuario usuario = usuarioRepository.findById(usuarioAutenticado.idUsuario())
				.orElseThrow(() -> {
					LOGGER.warn("Usuario autenticado nao encontrado{}. idUsuario={}",
							sufixoContexto(contextoOperacao),
							usuarioAutenticado.idUsuario());
					return new AuthenticatedUserInactiveOrNotFoundException();
				});

		if (usuario.estaInativo()) {
			LOGGER.warn("Usuario autenticado inativo{}. idUsuario={}",
					sufixoContexto(contextoOperacao),
					usuario.getIdUsuario());
			throw new AuthenticatedUserInactiveOrNotFoundException();
		}

		return usuario;
	}

	private String sufixoContexto(String contextoOperacao) {
		if (contextoOperacao == null || contextoOperacao.isBlank()) {
			return ".";
		}

		return " " + contextoOperacao + ".";
	}
}
