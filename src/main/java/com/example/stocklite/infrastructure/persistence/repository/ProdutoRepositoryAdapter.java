package com.example.stocklite.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.domain.repository.ProdutoRepository;
import com.example.stocklite.infrastructure.persistence.mapper.ProdutoPersistenceMapper;

@Repository
public class ProdutoRepositoryAdapter implements ProdutoRepository {

	private final SpringDataProdutoRepository springDataProdutoRepository;
	private final ProdutoPersistenceMapper produtoPersistenceMapper;

	public ProdutoRepositoryAdapter(
			SpringDataProdutoRepository springDataProdutoRepository,
			ProdutoPersistenceMapper produtoPersistenceMapper) {
		this.springDataProdutoRepository = springDataProdutoRepository;
		this.produtoPersistenceMapper = produtoPersistenceMapper;
	}

	@Override
	public Optional<Produto> findById(Integer idProduto) {
		return springDataProdutoRepository.findById(idProduto)
				.map(produtoPersistenceMapper::toDomain);
	}
}
