package com.example.stocklite.application.exception;

public class ProfileNotFoundException extends RuntimeException {

	public ProfileNotFoundException() {
		super("Perfil nao encontrado.");
	}
}
