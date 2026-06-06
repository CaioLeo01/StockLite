package com.example.stocklite.application.exception;

public class UserAccessDeniedException extends RuntimeException {
	private static final String MENSAGEM_PADRAO = "Usuario sem permissao para acessar o sistema.";

	public UserAccessDeniedException() {
		super(MENSAGEM_PADRAO);
	}

	public UserAccessDeniedException(String mensagem) {
		super(mensagem);
	}
}
