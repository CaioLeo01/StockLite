package com.example.stocklite.application.exception;

public class SelfUserUpdateNotAllowedException extends RuntimeException {

	public SelfUserUpdateNotAllowedException() {
		super("Usuario sem permissao para executar esta acao.");
	}
}
