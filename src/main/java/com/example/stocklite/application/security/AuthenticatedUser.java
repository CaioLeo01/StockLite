package com.example.stocklite.application.security;

public record AuthenticatedUser(
		Integer idUsuario,
		String email,
		String nivelPermissao) {
}
