package com.example.stocklite.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stocklite.infrastructure.persistence.entity.UsuarioEntity;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

	boolean existsByEmailIgnoreCase(String email);
}
