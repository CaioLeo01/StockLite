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
@Table(name = "usuario", schema = "stocklite")
public class UsuarioEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_usuario")
	private Integer idUsuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "perfil_fk", nullable = false)
	private PerfilEntity perfil;

	@Column(name = "nome", nullable = false, length = 100)
	private String nome;

	@Column(name = "email", nullable = false, length = 100)
	private String email;

	@Column(name = "senha", nullable = false, length = 255)
	private String senha;

	@Column(name = "ativo", nullable = false)
	private Boolean ativo;

	@Column(name = "dta_criacao", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataCriacao;

	@Column(name = "dta_update", nullable = false, updatable = false, insertable = false)
	private OffsetDateTime dataAtualizacao;
}
