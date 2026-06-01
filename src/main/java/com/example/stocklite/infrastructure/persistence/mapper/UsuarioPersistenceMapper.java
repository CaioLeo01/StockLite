package com.example.stocklite.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.infrastructure.persistence.entity.UsuarioEntity;

@Component
public class UsuarioPersistenceMapper {

	private final PerfilPersistenceMapper perfilPersistenceMapper;

	public UsuarioPersistenceMapper(PerfilPersistenceMapper perfilPersistenceMapper) {
		this.perfilPersistenceMapper = perfilPersistenceMapper;
	}

	public Usuario toDomain(UsuarioEntity entity) {
		if (entity == null) {
			return null;
		}

		return Usuario.builder()
				.idUsuario(entity.getIdUsuario())
				.perfil(perfilPersistenceMapper.toDomain(entity.getPerfil()))
				.nome(entity.getNome())
				.email(entity.getEmail())
				.senha(entity.getSenha())
				.ativo(entity.getAtivo())
				.dataCriacao(entity.getDataCriacao())
				.dataAtualizacao(entity.getDataAtualizacao())
				.build();
	}

	public UsuarioEntity toEntity(Usuario domain) {
		if (domain == null) {
			return null;
		}

		return UsuarioEntity.builder()
				.idUsuario(domain.getIdUsuario())
				.perfil(perfilPersistenceMapper.toEntity(domain.getPerfil()))
				.nome(domain.getNome())
				.email(domain.getEmail())
				.senha(domain.getSenha())
				.ativo(domain.getAtivo())
				.dataCriacao(domain.getDataCriacao())
				.dataAtualizacao(domain.getDataAtualizacao())
				.build();
	}
}
