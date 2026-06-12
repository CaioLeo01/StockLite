package com.example.stocklite.application.exception;

public class ProductAlreadyExistsException extends RuntimeException {

	private static final String MENSAGEM_PADRAO = "Ja existe um produto cadastrado com estes dados.";

	public ProductAlreadyExistsException() {
		super(MENSAGEM_PADRAO);
	}
}
