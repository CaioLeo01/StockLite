package com.example.stocklite.application.dto;

public record UsuarioResponse(
		Integer idUsuario,
		String nome,
		String email,
		String perfilNome,
		Boolean status) {
}
