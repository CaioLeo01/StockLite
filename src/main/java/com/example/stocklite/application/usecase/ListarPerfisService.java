package com.example.stocklite.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.PerfilResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.repository.PerfilRepository;

@Service
public class ListarPerfisService {

	private final PerfilRepository perfilRepository;
	private final AuthenticatedUserValidator authenticatedUserValidator;

	public ListarPerfisService(
			PerfilRepository perfilRepository,
			AuthenticatedUserValidator authenticatedUserValidator) {
		this.perfilRepository = perfilRepository;
		this.authenticatedUserValidator = authenticatedUserValidator;
	}

	public List<PerfilResponse> listar(AuthenticatedUser usuarioAutenticado) {
		authenticatedUserValidator.validarUsuarioAtivo(usuarioAutenticado, null);

		return perfilRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	private PerfilResponse toResponse(Perfil perfil) {
		return new PerfilResponse(
				perfil.getIdPerfil(),
				perfil.getNome(),
				perfil.getDescricao());
	}
}
