package com.example.stocklite.application.dto;

import java.math.BigDecimal;

public record ProdutoListagemResponse(
		Integer idProduto,
		String nome,
		BigDecimal preco) {
}
