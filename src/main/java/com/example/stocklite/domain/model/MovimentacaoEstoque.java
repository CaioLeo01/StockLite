package com.example.stocklite.domain.model;

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
public class MovimentacaoEstoque {

	private Integer idMovimentacao;

	private Produto produto;

	private Usuario usuario;

	private String tipo;

	private Integer quantidade;

	private OffsetDateTime dataMovimentacao;

	private String observacao;

	private OffsetDateTime dataCriacao;

	private OffsetDateTime dataAtualizacao;
}
