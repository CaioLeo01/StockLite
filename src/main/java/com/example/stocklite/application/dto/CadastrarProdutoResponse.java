package com.example.stocklite.application.dto;

import java.math.BigDecimal;

public record CadastrarProdutoResponse(
		Integer idProduto,
		String nome,
		String descricao,
		BigDecimal preco,
		Integer quantidadeAtual,
		Boolean status) {
}
