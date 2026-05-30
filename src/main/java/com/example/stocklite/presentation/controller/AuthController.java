package com.example.stocklite.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.RegisterRequest;
import com.example.stocklite.application.dto.RegisterResponse;
import com.example.stocklite.application.usecase.RegisterUserService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final RegisterUserService registerUserService;

	public AuthController(RegisterUserService registerUserService) {
		this.registerUserService = registerUserService;
	}

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> registrar(@Valid @RequestBody RegisterRequest request) {
		RegisterResponse response = registerUserService.registrar(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
