package com.example.stocklite.domain.repository;

import java.util.List;
import java.util.Optional;

import com.example.stocklite.domain.model.Usuario;

public interface UsuarioRepository {

	boolean existsByEmailIgnoreCase(String email);

	Optional<Usuario> findByEmailIgnoreCase(String email);

	Optional<Usuario> findById(Integer idUsuario);

	List<Usuario> findAll();

	Usuario save(Usuario usuario);
}
