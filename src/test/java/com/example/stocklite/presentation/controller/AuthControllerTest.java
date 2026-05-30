package com.example.stocklite.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.dto.RegisterResponse;
import com.example.stocklite.application.exception.EmailAlreadyInUseException;
import com.example.stocklite.application.usecase.RegisterUserService;
import com.example.stocklite.presentation.exception.GlobalExceptionHandler;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegisterUserService registerUserService;

	@Test
	void deveRegistrarUsuarioComSucesso() throws Exception {
		RegisterResponse response = new RegisterResponse(
				1,
				"Joao da Silva",
				"joao@email.com",
				"Usuario registrado com sucesso.");

		when(registerUserService.registrar(any())).thenReturn(response);

		mockMvc.perform(post("/v1/api/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Joao da Silva",
						  "email": "joao@email.com",
						  "senha": "SenhaForte@123"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.idUsuario").value(1))
				.andExpect(jsonPath("$.nome").value("Joao da Silva"))
				.andExpect(jsonPath("$.email").value("joao@email.com"))
				.andExpect(jsonPath("$.mensagem").value("Usuario registrado com sucesso."));
	}

	@Test
	void deveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
		mockMvc.perform(post("/v1/api/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "",
						  "email": "email-invalido",
						  "senha": "123"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").isString());
	}

	@Test
	void deveRetornarConflictQuandoEmailJaExistir() throws Exception {
		when(registerUserService.registrar(any())).thenThrow(new EmailAlreadyInUseException());

		mockMvc.perform(post("/v1/api/auth/register")
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Joao da Silva",
						  "email": "joao@email.com",
						  "senha": "SenhaForte@123"
						}
						"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value("Nao foi possivel concluir o cadastro com o email informado."));
	}
}
