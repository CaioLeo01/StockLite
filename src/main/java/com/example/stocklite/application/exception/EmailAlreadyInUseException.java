package com.example.stocklite.application.exception;

public class EmailAlreadyInUseException extends RuntimeException {

	public EmailAlreadyInUseException() {
		super("Nao foi possivel concluir o cadastro com o email informado.");
	}
}
