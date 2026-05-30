package com.example.stocklite.application.port;

public interface PasswordHasher {

	String hash(String rawPassword);
}
