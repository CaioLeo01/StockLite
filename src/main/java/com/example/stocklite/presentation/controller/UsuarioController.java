package com.example.stocklite.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.InactivateUserResult;
import com.example.stocklite.application.usecase.InactivateUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Validated
@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private static final String MENSAGEM_USUARIO_INATIVADO = "Usuario inativado com sucesso.";
	private static final String MENSAGEM_USUARIO_JA_INATIVO = "Usuario ja esta inativo.";

	private final InactivateUserService inactivateUserService;

	public UsuarioController(InactivateUserService inactivateUserService) {
		this.inactivateUserService = inactivateUserService;
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<MessageResponse> inativarUsuario(
			@PathVariable("id") Integer idUsuario,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		InactivateUserResult result = inactivateUserService.inativar(idUsuario, usuarioAutenticado);
		MessageResponse response = criarResposta(result);
		return ResponseEntity.ok(response);
	}

	private MessageResponse criarResposta(InactivateUserResult result) {
		if (result == InactivateUserResult.JA_ESTAVA_INATIVO) {
			return new MessageResponse(MENSAGEM_USUARIO_JA_INATIVO);
		}

		return new MessageResponse(MENSAGEM_USUARIO_INATIVADO);
	}
}
