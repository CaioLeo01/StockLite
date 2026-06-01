package com.example.stocklite.application.usecase;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.LoginRequest;
import com.example.stocklite.application.dto.LoginResponse;
import com.example.stocklite.application.exception.InvalidCredentialsException;
import com.example.stocklite.application.exception.UserAccessDeniedException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class LoginService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordHasher passwordHasher;
	private final TokenService tokenService;

	public LoginService(
			UsuarioRepository usuarioRepository,
			PasswordHasher passwordHasher,
			TokenService tokenService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordHasher = passwordHasher;
		this.tokenService = tokenService;
	}

	public LoginResponse autenticar(LoginRequest request) {
		String emailNormalizado = normalizarEmail(request.email());

		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.orElseThrow(() -> {
					LOGGER.warn("Tentativa de login com credenciais invalidas para o email {}", emailNormalizado);
					return new InvalidCredentialsException();
				});

		if (!passwordHasher.matches(request.senha(), usuario.getSenha())) {
			LOGGER.warn("Tentativa de login com credenciais invalidas para o email {}", emailNormalizado);
			throw new InvalidCredentialsException();
		}

		if (!Boolean.TRUE.equals(usuario.getAtivo())) {
			LOGGER.warn("Tentativa de login de usuario inativo. idUsuario={}", usuario.getIdUsuario());
			throw new UserAccessDeniedException();
		}

		validarPerfil(usuario.getPerfil());
		String token = tokenService.generateToken(usuario);

		return new LoginResponse(token);
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private void validarPerfil(Perfil perfil) {
		if (perfil == null || perfil.getNome() == null || perfil.getNome().isBlank()) {
			LOGGER.warn("Tentativa de login de usuario sem perfil valido.");
			throw new UserAccessDeniedException();
		}
	}
}
