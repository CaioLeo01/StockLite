package com.example.stocklite.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stocklite.application.dto.HealthResponse;
import com.example.stocklite.application.usecase.HealthService;

@RestController
@RequestMapping
public class HealthController {

	private final HealthService healthService;

	public HealthController(HealthService healthService) {
		this.healthService = healthService;
	}

	@GetMapping("/health")
	public ResponseEntity<HealthResponse> buscarSaudeAplicacao() {
		long inicioProcessamentoEmNanos = System.nanoTime();
		HealthResponse response = healthService.gerarStatusSaude(inicioProcessamentoEmNanos);
		return ResponseEntity.ok(response);
	}
}
