package com.example.stocklite.infrastructure.persistence.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "perfil", schema = "stocklite")
public class PerfilEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_perfil")
	private Integer idPerfil;

	@Column(name = "nome", nullable = false, length = 50)
	private String nome;

	@Column(name = "descricao", length = 255)
	private String descricao;

	@Column(name = "dta_criacao", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataCriacao;

	@Column(name = "dta_update", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataAtualizacao;
}
