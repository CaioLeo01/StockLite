package com.example.stocklite.application.exception;

public class SelfUserDeletionNotAllowedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SelfUserDeletionNotAllowedException() {
		super("Usuario sem permissao para executar esta acao.");
	}
}
