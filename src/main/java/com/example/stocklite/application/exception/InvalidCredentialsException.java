package com.example.stocklite.application.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Email ou senha invalidos.");
	}
}
