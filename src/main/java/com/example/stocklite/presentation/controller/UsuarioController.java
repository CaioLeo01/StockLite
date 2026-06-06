package com.example.stocklite.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.AtualizarUsuarioRequest;
import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.dto.UsuarioListagemResponse;
import com.example.stocklite.application.dto.UsuarioResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.InactivateUserResult;
import com.example.stocklite.application.usecase.InactivateUserService;
import com.example.stocklite.application.usecase.ListUsersService;
import com.example.stocklite.application.usecase.UpdateUserService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Validated
@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private static final String MENSAGEM_USUARIO_DELETADO = "Usuario deletado com sucesso.";
	private static final String MENSAGEM_USUARIO_JA_INATIVO = "Usuario ja esta inativo.";

	private final ListUsersService listUsersService;
	private final InactivateUserService inactivateUserService;
	private final UpdateUserService updateUserService;

	public UsuarioController(
			ListUsersService listUsersService,
			InactivateUserService inactivateUserService,
			UpdateUserService updateUserService) {
		this.listUsersService = listUsersService;
		this.inactivateUserService = inactivateUserService;
		this.updateUserService = updateUserService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UsuarioListagemResponse>> listarUsuarios(
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(listUsersService.listar(usuarioAutenticado));
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

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UsuarioResponse> atualizarUsuario(
			@PathVariable("id") Integer idUsuario,
			@Valid @RequestBody AtualizarUsuarioRequest request,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(updateUserService.atualizar(idUsuario, request, usuarioAutenticado));
	}

	private MessageResponse criarResposta(InactivateUserResult result) {
		if (result == InactivateUserResult.JA_ESTAVA_INATIVO) {
			return new MessageResponse(MENSAGEM_USUARIO_JA_INATIVO);
		}

		return new MessageResponse(MENSAGEM_USUARIO_DELETADO);
	}
}
