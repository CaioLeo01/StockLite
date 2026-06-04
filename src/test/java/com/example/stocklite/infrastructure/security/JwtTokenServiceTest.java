package com.example.stocklite.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JwtTokenServiceTest {

	@Test
	void deveGerarTokenComClaimsEsperadasEExpiracaoConfigurada() {
		String secret = "stocklite-dev-secret-key-change-me-2026";
		JwtTokenService jwtTokenService = new JwtTokenService(secret, 3600L);

		Perfil perfil = new Perfil();
		perfil.setNome("ADMIN");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(1);
		usuario.setEmail("joao@email.com");
		usuario.setPerfil(perfil);

		String token = jwtTokenService.generateToken(usuario);

		Claims claims = Jwts.parserBuilder()
				.setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
				.build()
				.parseClaimsJws(token)
				.getBody();

		assertNotNull(token);
		assertEquals("1", claims.getSubject());
		assertEquals("joao@email.com", claims.get("email", String.class));
		assertEquals("ADMIN", claims.get("nivelPermissao", String.class));
		assertEquals(3600L, (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000L);
	}

	@Test
	void deveExtrairUsuarioAutenticadoAPartirDoToken() {
		String secret = "stocklite-dev-secret-key-change-me-2026";
		JwtTokenService jwtTokenService = new JwtTokenService(secret, 3600L);

		Perfil perfil = new Perfil();
		perfil.setNome("ADMIN");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(7);
		usuario.setEmail("admin@email.com");
		usuario.setPerfil(perfil);

		String token = jwtTokenService.generateToken(usuario);
		AuthenticatedUser authenticatedUser = jwtTokenService.parseToken(token);

		assertEquals(7, authenticatedUser.idUsuario());
		assertEquals("admin@email.com", authenticatedUser.email());
		assertEquals("ADMIN", authenticatedUser.nivelPermissao());
	}

	@Test
	void deveRejeitarTokenExpirado() {
		String secret = "stocklite-dev-secret-key-change-me-2026";
		JwtTokenService jwtTokenService = new JwtTokenService(secret, -10L);

		Perfil perfil = new Perfil();
		perfil.setNome("ADMIN");

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(1);
		usuario.setEmail("joao@email.com");
		usuario.setPerfil(perfil);

		String token = jwtTokenService.generateToken(usuario);

		assertThrows(RuntimeException.class, () -> jwtTokenService.parseToken(token));
	}

	@Test
	void deveRejeitarTokenMalformado() {
		String secret = "stocklite-dev-secret-key-change-me-2026";
		JwtTokenService jwtTokenService = new JwtTokenService(secret, 3600L);

		assertThrows(RuntimeException.class, () -> jwtTokenService.parseToken("token-invalido"));
	}
}
