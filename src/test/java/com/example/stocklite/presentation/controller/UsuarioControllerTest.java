package com.example.stocklite.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.dto.UsuarioListagemResponse;
import com.example.stocklite.application.dto.UsuarioResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProfileNotFoundException;
import com.example.stocklite.application.exception.SelfUserUpdateNotAllowedException;
import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.exception.UserUpdateConflictException;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.application.usecase.InactivateUserResult;
import com.example.stocklite.application.usecase.InactivateUserService;
import com.example.stocklite.application.usecase.ListUsersService;
import com.example.stocklite.application.usecase.UpdateUserService;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataPerfilRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataUsuarioRepository;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
class UsuarioControllerTest {

	private static final String CONTEXT_PATH = "/v1/api";
	private static final String TOKEN_VALIDO = "jwt-valido";
	private static final String TOKEN_INVALIDO = "jwt-invalido";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InactivateUserService inactivateUserService;

	@MockitoBean
	private ListUsersService listUsersService;

	@MockitoBean
	private UpdateUserService updateUserService;

	@MockitoBean
	private TokenService tokenService;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private PerfilRepository perfilRepository;

	@MockitoBean
	private SpringDataUsuarioRepository springDataUsuarioRepository;

	@MockitoBean
	private SpringDataPerfilRepository springDataPerfilRepository;

	@MockitoBean
	private PasswordHasher passwordHasher;

	@Test
	void deveListarUsuariosQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listUsersService.listar(usuarioAutenticado)).thenReturn(List.of(
				new UsuarioListagemResponse(1, "Joao da Silva", "joao@email.com", 1, "ADMIN", Boolean.TRUE),
				new UsuarioListagemResponse(2, "Maria Souza", "maria@email.com", 2, "OPERADOR", Boolean.TRUE)));

		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].idUsuario").value(1))
				.andExpect(jsonPath("$[0].nome").value("Joao da Silva"))
				.andExpect(jsonPath("$[0].email").value("joao@email.com"))
				.andExpect(jsonPath("$[0].perfilId").value(1))
				.andExpect(jsonPath("$[0].perfilNome").value("ADMIN"))
				.andExpect(jsonPath("$[0].status").value(true))
				.andExpect(jsonPath("$[1].idUsuario").value(2))
				.andExpect(jsonPath("$[1].perfilNome").value("OPERADOR"));
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverUsuarios() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listUsersService.listar(usuarioAutenticado)).thenReturn(List.of());

		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void deveInativarUsuarioQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(2, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenReturn(InactivateUserResult.INATIVADO);

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Usuario deletado com sucesso."));
	}

	@Test
	void deveAtualizarUsuarioQuandoTokenForValidoEUsuarioForAdmin() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		UsuarioResponse response = new UsuarioResponse(
				2,
				"Maria Silva",
				"maria.silva@email.com",
				"OPERADOR",
				Boolean.FALSE);

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(2), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenReturn(response);

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": false
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.idUsuario").value(2))
				.andExpect(jsonPath("$.nome").value("Maria Silva"))
				.andExpect(jsonPath("$.email").value("maria.silva@email.com"))
				.andExpect(jsonPath("$.perfilNome").value("OPERADOR"))
				.andExpect(jsonPath("$.status").value(false));
	}

	@Test
	void deveRetornarMensagemInformativaQuandoUsuarioJaEstiverInativo() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(2, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenReturn(InactivateUserResult.JA_ESTAVA_INATIVO);

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mensagem").value("Usuario ja esta inativo."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenNaoForInformado() throws Exception {
		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": true
						}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarUnauthorizedQuandoTokenForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_INVALIDO)).thenThrow(new RuntimeException("Token invalido"));

		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_INVALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.mensagem").value("Token invalido ou nao informado."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPermissao() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(10, "viewer@email.com", "VISUALIZADOR"));

		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoForInexistenteOuInativoNaListagem() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(listUsersService.listar(usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(get("/v1/api/usuarios")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoForInexistenteOuInativoNaInativacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(inactivateUserService.inativar(2, usuarioAutenticado))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(delete("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoForInexistenteOuInativoNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(2), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new AuthenticatedUserInactiveOrNotFoundException());

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario autenticado inexistente ou inativo."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioTentarInativarASiMesmo() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));
		when(inactivateUserService.inativar(1, new AuthenticatedUser(1, "admin@email.com", "ADMIN")))
				.thenThrow(new SelfUserDeletionNotAllowedException());

		mockMvc.perform(delete("/v1/api/usuarios/1")
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.mensagem").value("Usuario sem permissao para executar esta acao."));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioTentarAtualizarASiMesmo() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new SelfUserUpdateNotAllowedException());

		mockMvc.perform(put("/v1/api/usuarios/1")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Admin",
						  "email": "admin@email.com",
						  "perfilId": 1,
						  "status": true
						}
						""")
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
				.contextPath(CONTEXT_PATH)
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Usuario nao encontrado."));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExistirNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(999), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new UserNotFoundException());

		mockMvc.perform(put("/v1/api/usuarios/999")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 2,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Usuario nao encontrado."));
	}

	@Test
	void deveRetornarNotFoundQuandoPerfilNaoExistirNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(2), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new ProfileNotFoundException());

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "maria.silva@email.com",
						  "perfilId": 999,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Perfil nao encontrado."));
	}

	@Test
	void deveRetornarConflictQuandoEmailJaEstiverEmUsoNaAtualizacao() throws Exception {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(tokenService.parseToken(TOKEN_VALIDO)).thenReturn(usuarioAutenticado);
		when(updateUserService.atualizar(org.mockito.ArgumentMatchers.eq(2), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(usuarioAutenticado)))
				.thenThrow(new UserUpdateConflictException());

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "Maria Silva",
						  "email": "joao@email.com",
						  "perfilId": 2,
						  "status": true
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.mensagem").value("Nao foi possivel atualizar o usuario com os dados informados."));
	}

	@Test
	void deveRetornarBadRequestQuandoPayloadDeAtualizacaoForInvalido() throws Exception {
		when(tokenService.parseToken(TOKEN_VALIDO))
				.thenReturn(new AuthenticatedUser(1, "admin@email.com", "ADMIN"));

		mockMvc.perform(put("/v1/api/usuarios/2")
				.contextPath(CONTEXT_PATH)
				.contentType(APPLICATION_JSON)
				.content("""
						{
						  "nome": "",
						  "email": "email-invalido",
						  "perfilId": null,
						  "status": null
						}
						""")
				.header(AUTHORIZATION, "Bearer " + TOKEN_VALIDO))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").isString());
	}
}
