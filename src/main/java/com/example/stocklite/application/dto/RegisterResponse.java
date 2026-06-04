package com.example.stocklite.application.dto;

public record RegisterResponse(
		Integer idUsuario,
		String nome,
		String email,
		String mensagem) {
}
