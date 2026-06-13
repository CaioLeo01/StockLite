package com.example.stocklite.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "movimentacao_estoque", schema = "stocklite")
public class MovimentacaoEstoqueEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_movimentacao_estoque")
	private Integer idMovimentacao;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_fk", nullable = false)
	private ProdutoEntity produto;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_fk", nullable = false)
	private UsuarioEntity usuario;

	@Column(name = "tipo", nullable = false, length = 20)
	private String tipo;

	@Column(name = "quantidade", nullable = false)
	private Integer quantidade;

	@Column(name = "data_movimentacao", nullable = false)
	private OffsetDateTime dataMovimentacao;

	@Column(name = "observacao", length = 255)
	private String observacao;

	@Column(name = "dta_criacao", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataCriacao;

	@Column(name = "dta_update", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataAtualizacao;
}
