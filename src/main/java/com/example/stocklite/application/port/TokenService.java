package com.example.stocklite.application.port;

import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Usuario;

public interface TokenService {

	String generateToken(Usuario usuario);

	AuthenticatedUser parseToken(String token);
}
