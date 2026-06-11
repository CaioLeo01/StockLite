package com.example.stocklite.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.stocklite.domain.model.Produto;

public interface ProdutoRepository {

	boolean existsByNomeIgnoreCase(String nome);

	Optional<Produto> findById(Integer idProduto);

	List<Produto> findAll();

	Produto save(Produto produto);
}
