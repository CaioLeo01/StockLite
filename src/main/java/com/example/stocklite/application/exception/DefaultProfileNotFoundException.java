package com.example.stocklite.application.exception;

public class DefaultProfileNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DefaultProfileNotFoundException() {
		super("Perfil padrao nao encontrado.");
	}
}
