package com.example.stocklite.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
		@NotBlank(message = "O nome e obrigatorio.")
		@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
		String nome,

		@NotBlank(message = "O email e obrigatorio.")
		@Email(message = "O email informado e invalido.")
		@Size(max = 100, message = "O email deve ter no maximo 100 caracteres.")
		String email,

		@NotNull(message = "O perfil e obrigatorio.")
		Integer perfilId,

		@NotNull(message = "O status e obrigatorio.")
		Boolean status) {
}
