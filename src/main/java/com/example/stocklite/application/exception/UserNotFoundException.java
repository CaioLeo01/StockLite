package com.example.stocklite.application.exception;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
		super("Usuario nao encontrado.");
	}
}
