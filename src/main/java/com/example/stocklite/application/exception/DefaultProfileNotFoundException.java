package com.example.stocklite.application.exception;

public class DefaultProfileNotFoundException extends RuntimeException {

	public DefaultProfileNotFoundException() {
		super("Perfil padrao nao encontrado.");
	}
}
