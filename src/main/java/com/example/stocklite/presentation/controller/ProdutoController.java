package com.example.stocklite.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.BuscarProdutoPorIdService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

	private final BuscarProdutoPorIdService buscarProdutoPorIdService;

	public ProdutoController(BuscarProdutoPorIdService buscarProdutoPorIdService) {
		this.buscarProdutoPorIdService = buscarProdutoPorIdService;
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA','VISUALIZADOR')")
	public ResponseEntity<ProdutoDetalheResponse> buscarPorId(
			@PathVariable("id") Integer idProduto,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(buscarProdutoPorIdService.buscar(idProduto, usuarioAutenticado));
	}
}
