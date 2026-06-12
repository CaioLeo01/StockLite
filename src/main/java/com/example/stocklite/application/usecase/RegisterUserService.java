package com.example.stocklite.application.usecase;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.RegisterRequest;
import com.example.stocklite.application.dto.RegisterResponse;
import com.example.stocklite.application.exception.DefaultProfileNotFoundException;
import com.example.stocklite.application.exception.EmailAlreadyInUseException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class RegisterUserService {

	private static final String PERFIL_PADRAO = "VISUALIZADOR";

	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final PasswordHasher passwordHasher;

	public RegisterUserService(
			UsuarioRepository usuarioRepository,
			PerfilRepository perfilRepository,
			PasswordHasher passwordHasher) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.passwordHasher = passwordHasher;
	}

	public RegisterResponse registrar(RegisterRequest request) {
		String emailNormalizado = normalizarEmail(request.email());

		if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
			throw new EmailAlreadyInUseException();
		}

		Perfil perfilPadrao = perfilRepository.findByNomeIgnoreCase(PERFIL_PADRAO)
				.orElseThrow(DefaultProfileNotFoundException::new);

		Usuario usuario = new Usuario();
		usuario.setPerfil(perfilPadrao);
		usuario.setNome(request.nome().trim());
		usuario.setEmail(emailNormalizado);
		usuario.setSenha(passwordHasher.hash(request.senha()));
		usuario.setAtivo(Boolean.TRUE);

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new RegisterResponse(
				usuarioSalvo.getIdUsuario(),
				usuarioSalvo.getNome(),
				usuarioSalvo.getEmail(),
				"Usuario registrado com sucesso.");
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
