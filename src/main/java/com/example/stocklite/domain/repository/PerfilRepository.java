package com.example.stocklite.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.stocklite.domain.model.Perfil;

public interface PerfilRepository {

	List<Perfil> findAll();

	Optional<Perfil> findById(Integer idPerfil);

	Optional<Perfil> findByNomeIgnoreCase(String nome);
}
