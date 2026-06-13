package com.example.stocklite.domain.repository;

import java.util.List;

import com.example.stocklite.domain.model.MovimentacaoEstoque;

public interface MovimentacaoRepository {

	List<MovimentacaoEstoque> findAllOrderByDataMovimentacaoDesc();
}
