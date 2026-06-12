package com.example.stocklite.infrastructure.persistence.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.example.stocklite.infrastructure.persistence.entity.UsuarioEntity;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

	boolean existsByEmailIgnoreCase(String email);

	@EntityGraph(attributePaths = "perfil")
	Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

	@Override
	@EntityGraph(attributePaths = "perfil")
	Optional<UsuarioEntity> findById(Integer idUsuario);

	@Override
	@EntityGraph(attributePaths = "perfil")
	List<UsuarioEntity> findAll();
}
