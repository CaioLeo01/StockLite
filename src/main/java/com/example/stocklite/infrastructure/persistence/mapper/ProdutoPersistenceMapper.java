package com.example.stocklite.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.example.stocklite.domain.model.Produto;
import com.example.stocklite.infrastructure.persistence.entity.ProdutoEntity;

@Component
public class ProdutoPersistenceMapper {

	public Produto toDomain(ProdutoEntity entity) {
		if (entity == null) {
			return null;
		}

		return Produto.builder()
				.idProduto(entity.getIdProduto())
				.nome(entity.getNome())
				.descricao(entity.getDescricao())
				.preco(entity.getPreco())
				.quantidadeEstoque(entity.getQuantidadeEstoque())
				.ativo(entity.getAtivo())
				.dataCriacao(entity.getDataCriacao())
				.dataAtualizacao(entity.getDataAtualizacao())
				.build();
	}

	public ProdutoEntity toEntity(Produto domain) {
		if (domain == null) {
			return null;
		}

		return ProdutoEntity.builder()
				.idProduto(domain.getIdProduto())
				.nome(domain.getNome())
				.descricao(domain.getDescricao())
				.preco(domain.getPreco())
				.quantidadeEstoque(domain.getQuantidadeEstoque())
				.ativo(domain.getAtivo())
				.dataCriacao(domain.getDataCriacao())
				.dataAtualizacao(domain.getDataAtualizacao())
				.build();
	}
}
