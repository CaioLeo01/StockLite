package com.example.stocklite.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIXO_BEARER = "Bearer ";

	private final TokenService tokenService;
	private final AuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(TokenService tokenService, AuthenticationEntryPoint authenticationEntryPoint) {
		this.tokenService = tokenService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!authorizationHeader.startsWith(PREFIXO_BEARER)) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(request, response, null);
			return;
		}

		String token = authorizationHeader.substring(PREFIXO_BEARER.length()).trim();

		if (token.isBlank()) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(request, response, null);
			return;
		}

		try {
			AuthenticatedUser authenticatedUser = tokenService.parseToken(token);
			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(
							authenticatedUser,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.nivelPermissao())));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (RuntimeException exception) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(request, response, null);
		}
	}
}
