package com.example.stocklite.application.usecase;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.example.stocklite.application.dto.HealthResponse;

@Service
public class HealthService {

	public HealthResponse gerarStatusSaude(long inicioProcessamentoEmNanos) {
		long tempoRespostaEmNanos = System.nanoTime() - inicioProcessamentoEmNanos;
		double tempoRespostaEmSegundos = tempoRespostaEmNanos / 1_000_000_000.0;

		return new HealthResponse(
				"UP",
				OffsetDateTime.now(),
				tempoRespostaEmSegundos);
	}
}
