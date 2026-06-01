package com.example.stocklite.infrastructure.persistence.repository;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.mapper.UsuarioPersistenceMapper;

@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

	private final SpringDataUsuarioRepository springDataUsuarioRepository;
	private final UsuarioPersistenceMapper usuarioPersistenceMapper;

	public UsuarioRepositoryAdapter(
			SpringDataUsuarioRepository springDataUsuarioRepository,
			UsuarioPersistenceMapper usuarioPersistenceMapper) {
		this.springDataUsuarioRepository = springDataUsuarioRepository;
		this.usuarioPersistenceMapper = usuarioPersistenceMapper;
	}

	@Override
	public boolean existsByEmailIgnoreCase(String email) {
		return springDataUsuarioRepository.existsByEmailIgnoreCase(email);
	}

	@Override
	public Optional<Usuario> findByEmailIgnoreCase(String email) {
		return springDataUsuarioRepository.findByEmailIgnoreCase(email)
				.map(usuarioPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Usuario> findById(Integer idUsuario) {
		return springDataUsuarioRepository.findById(idUsuario)
				.map(usuarioPersistenceMapper::toDomain);
	}

	@Override
	public Usuario save(Usuario usuario) {
		return usuarioPersistenceMapper.toDomain(
				springDataUsuarioRepository.save(usuarioPersistenceMapper.toEntity(usuario)));
	}
}
