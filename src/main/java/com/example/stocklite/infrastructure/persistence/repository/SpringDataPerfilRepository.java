package com.example.stocklite.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stocklite.infrastructure.persistence.entity.PerfilEntity;

public interface SpringDataPerfilRepository extends JpaRepository<PerfilEntity, Integer> {

	Optional<PerfilEntity> findByNomeIgnoreCase(String nome);
}
