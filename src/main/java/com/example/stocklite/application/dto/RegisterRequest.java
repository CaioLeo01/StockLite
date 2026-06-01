package com.example.stocklite.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "O nome e obrigatorio.")
		@Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
		String nome,

		@NotBlank(message = "O email e obrigatorio.")
		@Email(message = "O email informado e invalido.")
		@Size(max = 100, message = "O email deve ter no maximo 100 caracteres.")
		String email,

		@NotBlank(message = "A senha e obrigatoria.")
		@Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
		@Pattern(
				regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
				message = "A senha deve conter letra maiuscula, minuscula, numero e caractere especial.")
		String senha) {
}
