package com.example.stocklite.application.dto;

import java.time.OffsetDateTime;

public record MovimentacaoResponse(
		Integer idMovimentacao,
		Integer produtoId,
		String produtoNome,
		Integer usuarioId,
		String usuarioNome,
		String tipo,
		Integer quantidade,
		OffsetDateTime dataMovimentacao,
		String observacao) {
}
