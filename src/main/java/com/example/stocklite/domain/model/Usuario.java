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
public class Usuario {

	private Integer idUsuario;

	private Perfil perfil;

	private String nome;

	private String email;

	private String senha;

	private Boolean ativo;

	private OffsetDateTime dataCriacao;

	private OffsetDateTime dataAtualizacao;
}
