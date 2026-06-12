package com.example.stocklite.application.exception;

public class ProductNotFoundException extends RuntimeException {

	private static final String MENSAGEM_PADRAO = "Produto nao encontrado.";

	public ProductNotFoundException() {
		super(MENSAGEM_PADRAO);
	}
}
