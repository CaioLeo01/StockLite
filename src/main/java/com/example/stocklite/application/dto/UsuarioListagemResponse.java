package com.example.stocklite.application.dto;

public record UsuarioListagemResponse(
		Integer idUsuario,
		String nome,
		String email,
		Integer perfilId,
		String perfilNome,
		Boolean status) {
}
