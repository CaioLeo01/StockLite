package com.example.stocklite.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistrarEntradaRequest(
		@NotNull(message = "O produto e obrigatorio.")
		Integer produtoId,

		@NotNull(message = "A quantidade e obrigatoria.")
		@Positive(message = "A quantidade deve ser maior que zero.")
		Integer quantidade,

		@Size(max = 255, message = "A observacao deve ter no maximo 255 caracteres.")
		String observacao) {
}
