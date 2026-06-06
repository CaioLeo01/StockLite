package com.example.stocklite.application.exception;

public class UserUpdateConflictException extends RuntimeException {

	public UserUpdateConflictException() {
		super("Nao foi possivel atualizar o usuario com os dados informados.");
	}
}
