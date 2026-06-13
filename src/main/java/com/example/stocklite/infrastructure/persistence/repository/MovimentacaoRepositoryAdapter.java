package com.example.stocklite.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.domain.repository.MovimentacaoRepository;
import com.example.stocklite.infrastructure.persistence.mapper.MovimentacaoPersistenceMapper;

@Repository
public class MovimentacaoRepositoryAdapter implements MovimentacaoRepository {

	private final SpringDataMovimentacaoRepository springDataMovimentacaoRepository;
	private final MovimentacaoPersistenceMapper movimentacaoPersistenceMapper;

	public MovimentacaoRepositoryAdapter(
			SpringDataMovimentacaoRepository springDataMovimentacaoRepository,
			MovimentacaoPersistenceMapper movimentacaoPersistenceMapper) {
		this.springDataMovimentacaoRepository = springDataMovimentacaoRepository;
		this.movimentacaoPersistenceMapper = movimentacaoPersistenceMapper;
	}

	@Override
	public List<MovimentacaoEstoque> findAllOrderByDataMovimentacaoDesc() {
		return springDataMovimentacaoRepository.findAllOrderByDataMovimentacaoDesc().stream()
				.map(movimentacaoPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public MovimentacaoEstoque save(MovimentacaoEstoque movimentacao) {
		return movimentacaoPersistenceMapper.toDomain(
				springDataMovimentacaoRepository.save(movimentacaoPersistenceMapper.toEntity(movimentacao)));
	}
}
