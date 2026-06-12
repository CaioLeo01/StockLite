package com.example.stocklite.application.exception;

public class SelfUserDeletionNotAllowedException extends RuntimeException {

	public SelfUserDeletionNotAllowedException() {
		super("Usuario sem permissao para executar esta acao.");
	}
}
