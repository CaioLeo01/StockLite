package com.example.stocklite.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.PerfilResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.ListarPerfisService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/perfis")
@SecurityRequirement(name = "bearerAuth")
public class PerfilController {

	private final ListarPerfisService listarPerfisService;

	public PerfilController(ListarPerfisService listarPerfisService) {
		this.listarPerfisService = listarPerfisService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<PerfilResponse>> listar(
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(listarPerfisService.listar(usuarioAutenticado));
	}
}
