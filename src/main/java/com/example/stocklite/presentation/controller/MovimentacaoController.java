package com.example.stocklite.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.MovimentacaoResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.ListarMovimentacoesService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/movimentacoes")
@SecurityRequirement(name = "bearerAuth")
public class MovimentacaoController {

	private final ListarMovimentacoesService listarMovimentacoesService;

	public MovimentacaoController(ListarMovimentacoesService listarMovimentacoesService) {
		this.listarMovimentacoesService = listarMovimentacoesService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA','VISUALIZADOR')")
	public ResponseEntity<List<MovimentacaoResponse>> listar(
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(listarMovimentacoesService.listar(usuarioAutenticado));
	}
}
