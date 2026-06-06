package com.example.stocklite.application.exception;

public class AuthenticatedUserInactiveOrNotFoundException extends RuntimeException {
	
	private static final String MENSAGEM_PADRAO = "Usuario autenticado inexistente ou inativo.";

	public AuthenticatedUserInactiveOrNotFoundException() {
		super(MENSAGEM_PADRAO);
	}
}
