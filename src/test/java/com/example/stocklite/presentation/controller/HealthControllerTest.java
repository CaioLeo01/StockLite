package com.example.stocklite.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.usecase.HealthService;

@WebMvcTest(HealthController.class)
@Import(HealthService.class)
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveRetornarSaudeDaAplicacaoNoPadraoDaApi() throws Exception {
		mockMvc.perform(get("/v1/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.dataHora").exists())
				.andExpect(jsonPath("$.tempoRespostaSegundos").isNumber());
	}

	@Test
	void naoDeveAceitarRotaHealt() throws Exception {
		mockMvc.perform(get("/v1/api/healt"))
				.andExpect(status().isNotFound());
	}
}
