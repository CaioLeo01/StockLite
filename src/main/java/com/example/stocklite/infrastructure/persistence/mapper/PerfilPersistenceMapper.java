package com.example.stocklite.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.infrastructure.persistence.entity.PerfilEntity;

@Component
public class PerfilPersistenceMapper {

	public Perfil toDomain(PerfilEntity entity) {
		if (entity == null) {
			return null;
		}

		return Perfil.builder()
				.idPerfil(entity.getIdPerfil())
				.nome(entity.getNome())
				.descricao(entity.getDescricao())
				.dataCriacao(entity.getDataCriacao())
				.dataAtualizacao(entity.getDataAtualizacao())
				.build();
	}

	public PerfilEntity toEntity(Perfil domain) {
		if (domain == null) {
			return null;
		}

		return PerfilEntity.builder()
				.idPerfil(domain.getIdPerfil())
				.nome(domain.getNome())
				.descricao(domain.getDescricao())
				.dataCriacao(domain.getDataCriacao())
				.dataAtualizacao(domain.getDataAtualizacao())
				.build();
	}
}
