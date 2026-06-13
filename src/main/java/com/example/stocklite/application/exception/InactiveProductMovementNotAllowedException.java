package com.example.stocklite.application.exception;

public class InactiveProductMovementNotAllowedException extends RuntimeException {

	private static final String MENSAGEM_PADRAO = "Produto inativo nao pode receber movimentacao.";

	public InactiveProductMovementNotAllowedException() {
		super(MENSAGEM_PADRAO);
	}
}
