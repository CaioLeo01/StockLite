package com.example.stocklite.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.stocklite.infrastructure.persistence.entity.MovimentacaoEstoqueEntity;

public interface SpringDataMovimentacaoRepository extends JpaRepository<MovimentacaoEstoqueEntity, Integer> {

	@Query("""
			select movimentacao
			from MovimentacaoEstoqueEntity movimentacao
			join fetch movimentacao.produto
			join fetch movimentacao.usuario usuario
			join fetch usuario.perfil
			order by movimentacao.dataMovimentacao desc, movimentacao.idMovimentacao desc
			""")
	List<MovimentacaoEstoqueEntity> findAllOrderByDataMovimentacaoDesc();
}
