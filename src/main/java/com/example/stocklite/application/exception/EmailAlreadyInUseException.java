package com.example.stocklite.application.exception;

public class EmailAlreadyInUseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailAlreadyInUseException() {
		super("Nao foi possivel concluir o cadastro com o email informado.");
	}
}
