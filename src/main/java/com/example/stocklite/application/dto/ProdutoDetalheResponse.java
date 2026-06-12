package com.example.stocklite.application.dto;

import java.math.BigDecimal;

public record ProdutoDetalheResponse(
		Integer idProduto,
		String nome,
		String descricao,
		Integer quantidadeEstoque,
		BigDecimal preco,
		Boolean status) {
}
