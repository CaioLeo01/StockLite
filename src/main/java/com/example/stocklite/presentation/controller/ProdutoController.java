package com.example.stocklite.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.CadastrarProdutoRequest;
import com.example.stocklite.application.dto.CadastrarProdutoResponse;
import com.example.stocklite.application.dto.ProdutoDetalheResponse;
import com.example.stocklite.application.dto.ProdutoListagemResponse;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.BuscarProdutoPorIdService;
import com.example.stocklite.application.usecase.CadastrarProdutoService;
import com.example.stocklite.application.usecase.ListarProdutosService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProdutoController {

	private final BuscarProdutoPorIdService buscarProdutoPorIdService;
	private final CadastrarProdutoService cadastrarProdutoService;
	private final ListarProdutosService listarProdutosService;

	public ProdutoController(
			BuscarProdutoPorIdService buscarProdutoPorIdService,
			CadastrarProdutoService cadastrarProdutoService,
			ListarProdutosService listarProdutosService) {
		this.buscarProdutoPorIdService = buscarProdutoPorIdService;
		this.cadastrarProdutoService = cadastrarProdutoService;
		this.listarProdutosService = listarProdutosService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA')")
	public ResponseEntity<CadastrarProdutoResponse> cadastrar(
			@Valid @RequestBody CadastrarProdutoRequest request,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(cadastrarProdutoService.cadastrar(request, usuarioAutenticado));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA','VISUALIZADOR')")
	public ResponseEntity<List<ProdutoListagemResponse>> listar(
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(listarProdutosService.listar(usuarioAutenticado));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','ESTOQUISTA','VISUALIZADOR')")
	public ResponseEntity<ProdutoDetalheResponse> buscarPorId(
			@PathVariable("id") Integer idProduto,
			@AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
		return ResponseEntity.ok(buscarProdutoPorIdService.buscar(idProduto, usuarioAutenticado));
	}
}
