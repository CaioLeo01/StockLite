package com.example.stocklite.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.usecase.HealthService;
import com.example.stocklite.infrastructure.security.JwtAuthenticationFilter;
import com.example.stocklite.infrastructure.security.RestAccessDeniedHandler;
import com.example.stocklite.infrastructure.security.RestAuthenticationEntryPoint;

@WebMvcTest(
		value = HealthController.class,
		excludeFilters = {
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RestAuthenticationEntryPoint.class),
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RestAccessDeniedHandler.class)
		})
@Import(HealthService.class)
class HealthControllerTest {

	private static final String CONTEXT_PATH = "/v1/api";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveRetornarSaudeDaAplicacaoNoPadraoDaApi() throws Exception {
		mockMvc.perform(get("/v1/api/health")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.dataHora").exists())
				.andExpect(jsonPath("$.tempoRespostaSegundos").isNumber());
	}

	@Test
	void naoDeveAceitarRotaHealt() throws Exception {
		mockMvc.perform(get("/v1/api/healt")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isNotFound());
	}
}
