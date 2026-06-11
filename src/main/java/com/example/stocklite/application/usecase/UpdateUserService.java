package com.example.stocklite.application.usecase;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.AtualizarUsuarioRequest;
import com.example.stocklite.application.dto.UsuarioResponse;
import com.example.stocklite.application.exception.ProfileNotFoundException;
import com.example.stocklite.application.exception.SelfUserUpdateNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.exception.UserUpdateConflictException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@Service
public class UpdateUserService {

	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public UpdateUserService(
			UsuarioRepository usuarioRepository,
			PerfilRepository perfilRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public UsuarioResponse atualizar(
			Integer idUsuarioAlvo,
			AtualizarUsuarioRequest request,
			AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, null);
		validarAutoAtualizacao(idUsuarioAlvo, usuarioAutenticado);

		Usuario usuario = usuarioRepository.findById(idUsuarioAlvo)
				.orElseThrow(UserNotFoundException::new);

		Perfil perfil = perfilRepository.findById(request.perfilId())
				.orElseThrow(ProfileNotFoundException::new);

		String emailNormalizado = normalizarEmail(request.email());
		validarConflitoEmail(idUsuarioAlvo, emailNormalizado);

		usuario.setNome(request.nome().trim());
		usuario.setEmail(emailNormalizado);
		usuario.setPerfil(perfil);
		usuario.setAtivo(request.status());

		Usuario usuarioAtualizado = usuarioRepository.save(usuario);
		return toResponse(usuarioAtualizado);
	}

	private void validarAutoAtualizacao(Integer idUsuarioAlvo, AuthenticatedUser usuarioAutenticado) {
		if (idUsuarioAlvo.equals(usuarioAutenticado.idUsuario())) {
			throw new SelfUserUpdateNotAllowedException();
		}
	}

	private void validarConflitoEmail(Integer idUsuarioAlvo, String emailNormalizado) {
		usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
				.filter(usuario -> !idUsuarioAlvo.equals(usuario.getIdUsuario()))
				.ifPresent(usuario -> {
					throw new UserUpdateConflictException();
				});
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private UsuarioResponse toResponse(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getIdUsuario(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getPerfil().getNome(),
				usuario.getAtivo());
	}
}
