package com.example.stocklite.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Produto {

	private Integer idProduto;

	private String nome;

	private String descricao;

	private BigDecimal preco;

	private Integer quantidadeEstoque;

	private Boolean ativo;

	private OffsetDateTime dataCriacao;

	private OffsetDateTime dataAtualizacao;

	public void inativar() {
		this.ativo = Boolean.FALSE;
	}

	public boolean estaInativo() {
		return !Boolean.TRUE.equals(this.ativo);
	}
}
