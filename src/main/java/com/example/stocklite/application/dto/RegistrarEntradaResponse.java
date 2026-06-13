package com.example.stocklite.application.dto;

public record RegistrarEntradaResponse(
		Integer idMovimentacao,
		Integer produtoId,
		String produtoNome,
		Integer quantidadeAtual) {
}
