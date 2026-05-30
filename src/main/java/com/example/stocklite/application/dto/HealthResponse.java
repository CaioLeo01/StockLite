package com.example.stocklite.application.dto;

import java.time.OffsetDateTime;

public record HealthResponse(
		String status,
		OffsetDateTime dataHora,
		double tempoRespostaSegundos) {
}
