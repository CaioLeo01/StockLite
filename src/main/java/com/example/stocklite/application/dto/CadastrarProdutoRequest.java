package com.example.stocklite.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CadastrarProdutoRequest(
		@NotBlank(message = "O nome e obrigatorio.")
		@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
		String nome,

		@Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres.")
		String descricao,

		@NotNull(message = "O preco e obrigatorio.")
		@Positive(message = "O preco deve ser maior que zero.")
		BigDecimal preco,

		@NotNull(message = "A quantidade inicial e obrigatoria.")
		@Min(value = 0, message = "A quantidade inicial nao pode ser negativa.")
		Integer quantidadeInicial) {
}
