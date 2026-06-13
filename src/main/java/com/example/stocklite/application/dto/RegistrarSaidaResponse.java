package com.example.stocklite.application.dto;

public record RegistrarSaidaResponse(
		Integer idMovimentacao,
		Integer produtoId,
		String produtoNome,
		Integer quantidadeAtual) {
}
