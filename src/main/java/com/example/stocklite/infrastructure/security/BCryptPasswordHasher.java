package com.example.stocklite.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.stocklite.application.port.PasswordHasher;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;

	public BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}
}
