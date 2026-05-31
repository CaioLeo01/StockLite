package com.example.stocklite.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.domain.model.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenService implements TokenService {

	private final SecretKey secretKey;
	private final long expirationInSeconds;

	public JwtTokenService(
			@Value("${app.jwt.secret}") String jwtSecret,
			@Value("${app.jwt.expiration-seconds}") long expirationInSeconds) {
		this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		this.expirationInSeconds = expirationInSeconds;
	}

	@Override
	public String generateToken(Usuario usuario) {
		Instant issuedAt = Instant.now();
		Instant expiration = issuedAt.plusSeconds(expirationInSeconds);

		return Jwts.builder()
				.setSubject(String.valueOf(usuario.getIdUsuario()))
				.claim("email", usuario.getEmail())
				.claim("nivelPermissao", usuario.getPerfil().getNome())
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiration))
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}

}
