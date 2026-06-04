package com.example.stocklite.application.exception;

public class InvalidCredentialsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException() {
		super("Email ou senha invalidos.");
	}
}
