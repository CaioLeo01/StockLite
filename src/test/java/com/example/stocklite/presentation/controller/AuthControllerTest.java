package com.example.stocklite.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.dto.RegisterResponse;
import com.example.stocklite.application.dto.LoginResponse;
import com.example.stocklite.application.exception.EmailAlreadyInUseException;
import com.example.stocklite.application.exception.InvalidCredentialsException;
import com.example.stocklite.application.exception.UserAccessDeniedException;
import com.example.stocklite.application.usecase.LoginService;
import com.example.stocklite.application.usecase.RegisterUserService;
import com.example.stocklite.infrastructure.security.JwtAuthenticationFilter;
import com.example.stocklite.infrastructure.security.RestAccessDeniedHandler;
import com.example.stocklite.infrastructure.security.RestAuthenticationEntryPoint;
import com.example.stocklite.presentation.exception.GlobalExceptionHandler;

@WebMvcTest(
		value = AuthController.class,
		excludeFilters = {
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RestAuthenticationEntryPoint.class),
				@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RestAccessDeniedHandler.class)
		})
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

	private static final String CONTEXT_PATH = "/v1/api";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegisterUserService registerUserService;

	@MockitoBean
	private LoginService loginService;

	@Test
	void deveRegistrarUsuarioComSucesso() throws Exception {
		RegisterResponse response = new RegisterResponse(
				1,
				"Joao da Silva",
				"joao@email.com",
				"Usuario registrado com sucesso.");

		when(registerUserService.registrar(any())).thenReturn(response);

		mockMvc.perform(post("/v1/api/auth/register")
				.contextPath(CONTEXT_PATH)
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
				.contextPath(CONTEXT_PATH)
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
				.contextPath(CONTEXT_PATH)
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

	@Test
	void deveAutenticarUsuarioComSucesso() throws Exception {
		LoginResponse response = new LoginResponse("Bearer jwt-gerado");

		when(loginService.autenticar(any())).thenReturn(response);

		mockMvc.perform(post("/v1/api/auth/login")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "email": "joao@email.com",
						  "senha": "SenhaForte@123"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("Bearer jwt-gerado"));
	}

	@Test
	void deveRetornarBadRequestQuandoPayloadDeLoginForInvalido() throws Exception {
		mockMvc.perform(post("/v1/api/auth/login")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "email": "email-invalido",
						  "senha": ""
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").isString());
	}

	@Test
	void deveRetornarUnauthorizedQuandoCredenciaisForemInvalidas() throws Exception {
		when(loginService.autenticar(any())).thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/v1/api/auth/login")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "email": "joao@email.com",
						  "senha": "SenhaErrada@123"
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Email ou senha invalidos."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverAcesso() throws Exception {
		when(loginService.autenticar(any())).thenThrow(new UserAccessDeniedException());

		mockMvc.perform(post("/v1/api/auth/login")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "email": "joao@email.com",
						  "senha": "SenhaForte@123"
						}
						"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para acessar o sistema."));
	}
}
