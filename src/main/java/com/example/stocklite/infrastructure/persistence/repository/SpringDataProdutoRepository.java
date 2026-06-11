package com.example.stocklite.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stocklite.infrastructure.persistence.entity.ProdutoEntity;

public interface SpringDataProdutoRepository extends JpaRepository<ProdutoEntity, Integer> {

	boolean existsByNomeIgnoreCase(String nome);
}
