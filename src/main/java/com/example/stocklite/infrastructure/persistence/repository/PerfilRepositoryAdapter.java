package com.example.stocklite.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.infrastructure.persistence.mapper.PerfilPersistenceMapper;

@Repository
public class PerfilRepositoryAdapter implements PerfilRepository {

	private final SpringDataPerfilRepository springDataPerfilRepository;
	private final PerfilPersistenceMapper perfilPersistenceMapper;

	public PerfilRepositoryAdapter(
			SpringDataPerfilRepository springDataPerfilRepository,
			PerfilPersistenceMapper perfilPersistenceMapper) {
		this.springDataPerfilRepository = springDataPerfilRepository;
		this.perfilPersistenceMapper = perfilPersistenceMapper;
	}

	@Override
	public List<Perfil> findAll() {
		return springDataPerfilRepository.findAll().stream()
				.map(perfilPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public Optional<Perfil> findById(Integer idPerfil) {
		return springDataPerfilRepository.findById(idPerfil)
				.map(perfilPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Perfil> findByNomeIgnoreCase(String nome) {
		return springDataPerfilRepository.findByNomeIgnoreCase(nome)
				.map(perfilPersistenceMapper::toDomain);
	}
}
