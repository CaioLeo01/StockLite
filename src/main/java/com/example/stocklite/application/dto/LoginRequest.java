package com.example.stocklite.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank(message = "O email e obrigatorio.")
		@Email(message = "O email informado e invalido.")
		@Size(max = 100, message = "O email deve ter no maximo 100 caracteres.")
		String email,

		@NotBlank(message = "A senha e obrigatoria.")
		@Size(max = 100, message = "A senha deve ter no maximo 100 caracteres.")
		String senha) {
}
