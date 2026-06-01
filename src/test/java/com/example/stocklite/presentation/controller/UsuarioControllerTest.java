package com.example.stocklite.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.InactivateUserResult;
import com.example.stocklite.application.usecase.InactivateUserService;
import com.example.stocklite.infrastructure.config.SecurityConfig;
import com.example.stocklite.infrastructure.security.JwtAuthenticationFilter;
import com.example.stocklite.infrastructure.security.RestAccessDeniedHandler;
import com.example.stocklite.infrastructure.security.RestAuthenticationEntryPoint;
import com.example.stocklite.presentation.exception.GlobalExceptionHandler;

@WebMvcTest(UsuarioController.class)
@Import({
		GlobalExceptionHandler.class,
		SecurityConfig.class,
		JwtAuthenticationFilter.class,
		RestAuthenticationEntryPoint.class,
		RestAccessDeniedHandler.class
})
class UsuarioControllerTest {

	private static final String TOKEN_VALIDO = "jwt-valido";
	private static final String TOKEN_INVALIDO = "jwt-invalido";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InactivateUserService inactivateUserService;

	@MockitoBean
	private TokenService tokenService;

	@Test
	void deveInativarUsuarioQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(2, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenReturn(InactivateUserResult.INATIVADO);

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Usuario inativado com sucesso."));
	}

	@Test
	void deveRetornarMensagemInformativaQuandoUsuarioJaEstiverInativo() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(2, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenReturn(InactivateUserResult.JA_ESTAVA_INATIVO);

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Usuario ja esta inativo."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformado() throws Exception {
		mockMvc.perform(delete("/v1/api/usuarios/2"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "viewer@email.com", "VISUALIZADOR"));

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioTentarInativarASiMesmo() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(1, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenThrow(new SelfUserDeletionNotAllowedException());

		mockMvc.perform(delete("/v1/api/usuarios/1")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExistir() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(999, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenThrow(new UserNotFoundException());

		mockMvc.perform(delete("/v1/api/usuarios/999")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Usuario nao encontrado."));
	}
}
