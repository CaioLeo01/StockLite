package com.example.stocklite.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.example.stocklite.domain.model.MovimentacaoEstoque;
import com.example.stocklite.infrastructure.persistence.entity.MovimentacaoEstoqueEntity;

@Component
public class MovimentacaoPersistenceMapper {

	private final ProdutoPersistenceMapper produtoPersistenceMapper;
	private final UsuarioPersistenceMapper usuarioPersistenceMapper;

	public MovimentacaoPersistenceMapper(
			ProdutoPersistenceMapper produtoPersistenceMapper,
			UsuarioPersistenceMapper usuarioPersistenceMapper) {
		this.produtoPersistenceMapper = produtoPersistenceMapper;
		this.usuarioPersistenceMapper = usuarioPersistenceMapper;
	}

	public MovimentacaoEstoque toDomain(MovimentacaoEstoqueEntity entity) {
		if (entity == null) {
			return null;
		}

		return MovimentacaoEstoque.builder()
				.idMovimentacao(entity.getIdMovimentacao())
				.produto(produtoPersistenceMapper.toDomain(entity.getProduto()))
				.usuario(usuarioPersistenceMapper.toDomain(entity.getUsuario()))
				.tipo(entity.getTipo())
				.quantidade(entity.getQuantidade())
				.dataMovimentacao(entity.getDataMovimentacao())
				.observacao(entity.getObservacao())
				.dataCriacao(entity.getDataCriacao())
				.dataAtualizacao(entity.getDataAtualizacao())
				.build();
	}

	public MovimentacaoEstoqueEntity toEntity(MovimentacaoEstoque domain) {
		if (domain == null) {
			return null;
		}

		return MovimentacaoEstoqueEntity.builder()
				.idMovimentacao(domain.getIdMovimentacao())
				.produto(produtoPersistenceMapper.toEntity(domain.getProduto()))
				.usuario(usuarioPersistenceMapper.toEntity(domain.getUsuario()))
				.tipo(domain.getTipo())
				.quantidade(domain.getQuantidade())
				.dataMovimentacao(domain.getDataMovimentacao())
				.observacao(domain.getObservacao())
				.dataCriacao(domain.getDataCriacao())
				.dataAtualizacao(domain.getDataAtualizacao())
				.build();
	}
}
