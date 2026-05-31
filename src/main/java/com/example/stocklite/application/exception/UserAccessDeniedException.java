package com.example.stocklite.application.exception;

public class UserAccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UserAccessDeniedException() {
		super("Usuario sem permissao para acessar o sistema.");
	}
}
