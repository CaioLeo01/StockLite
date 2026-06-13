package com.example.stocklite.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.MovimentacaoResponse;
import com.example.stocklite.application.dto.RegistrarEntradaRequest;
import com.example.stocklite.application.dto.RegistrarEntradaResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.ListarMovimentacoesService;
import com.example.stocklite.application.usecase.RegistrarEntradaEstoqueService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/movimentacoes")
@SecurityRequirement(name = "bearerAuth")
public class MovimentacaoController {

	private final ListarMovimentacoesService listarMovimentacoesService;
	private final RegistrarEntradaEstoqueService registrarEntradaEstoqueService;

	public MovimentacaoController(
			ListarMovimentacoesService listarMovimentacoesService,
			RegistrarEntradaEstoqueService registrarEntradaEstoqueService) {
		this.listarMovimentacoesService = listarMovimentacoesService;
		this.registrarEntradaEstoqueService = registrarEntradaEstoqueService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA','VISUALIZADOR')")
	public ResponseEntity<List<MovimentacaoResponse>> listar(
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(listarMovimentacoesService.listar(usuarioAutenticado));
	}

	@PostMapping("/entrada")
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA')")
	public ResponseEntity<RegistrarEntradaResponse> registrarEntrada(
			@Valid @RequestBody RegistrarEntradaRequest request,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(registrarEntradaEstoqueService.registrar(request, usuarioAutenticado));
	}
}
