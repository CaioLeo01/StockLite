package com.example.stocklite.domain.repository;

import java.util.Optional;

import com.example.stocklite.domain.model.Perfil;

public interface PerfilRepository {

	Optional<Perfil> findByNomeIgnoreCase(String nome);
}
