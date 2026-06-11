package com.example.stocklite.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.stocklite.domain.model.Produto;

public interface ProdutoRepository {

	Optional<Produto> findById(Integer idProduto);

	List<Produto> findAll();
}
