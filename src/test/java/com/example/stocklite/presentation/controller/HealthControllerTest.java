package com.example.stocklite.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.usecase.HealthService;
import com.example.stocklite.infrastructure.config.SecurityConfig;
import com.example.stocklite.infrastructure.security.JwtAuthenticationFilter;
import com.example.stocklite.infrastructure.security.RestAccessDeniedHandler;
import com.example.stocklite.infrastructure.security.RestAuthenticationEntryPoint;

@WebMvcTest(HealthController.class)
@Import({
		HealthService.class,
		SecurityConfig.class,
		JwtAuthenticationFilter.class,
		RestAuthenticationEntryPoint.class,
		RestAccessDeniedHandler.class
})
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TokenService tokenService;

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
