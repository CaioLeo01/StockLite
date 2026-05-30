package com.example.stocklite.domain.repository;

import com.example.stocklite.domain.model.Usuario;

public interface UsuarioRepository {

	boolean existsByEmailIgnoreCase(String email);

	Usuario save(Usuario usuario);
}
