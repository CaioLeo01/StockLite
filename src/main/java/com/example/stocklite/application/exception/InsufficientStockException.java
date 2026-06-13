package com.example.stocklite.application.exception;

public class InsufficientStockException extends RuntimeException {

	private static final String MENSAGEM_PADRAO = "Quantidade insuficiente em estoque.";

	public InsufficientStockException() {
		super(MENSAGEM_PADRAO);
	}
}
