package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.UsuarioListagemResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private ListUsersService listUsersService;

	private Perfil perfilAdmin;
	private Usuario usuarioOperadorAtivo;
	private Usuario usuarioOperadorInativo;

	@BeforeEach
	void setUp() {
		perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

		Perfil perfilOperador = new Perfil();
		perfilOperador.setIdPerfil(2);
		perfilOperador.setNome("OPERADOR");

		usuarioOperadorAtivo = new Usuario();
		usuarioOperadorAtivo.setIdUsuario(2);
		usuarioOperadorAtivo.setNome("Maria Souza");
		usuarioOperadorAtivo.setEmail("maria@email.com");
		usuarioOperadorAtivo.setSenha("hash-maria");
		usuarioOperadorAtivo.setAtivo(Boolean.TRUE);
		usuarioOperadorAtivo.setPerfil(perfilOperador);

		usuarioOperadorInativo = new Usuario();
		usuarioOperadorInativo.setIdUsuario(3);
		usuarioOperadorInativo.setNome("Carlos Lima");
		usuarioOperadorInativo.setEmail("carlos@email.com");
		usuarioOperadorInativo.setSenha("hash-carlos");
		usuarioOperadorInativo.setAtivo(Boolean.FALSE);
		usuarioOperadorInativo.setPerfil(perfilOperador);
	}

	@Test
	void deveListarUsuariosAtivosEInativosQuandoAutenticadoForValidoEAtivo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		Usuario usuarioAdminAtivo = new Usuario();
		usuarioAdminAtivo.setIdUsuario(1);
		usuarioAdminAtivo.setNome("Administrador");
		usuarioAdminAtivo.setEmail("admin@email.com");
		usuarioAdminAtivo.setSenha("hash-admin");
		usuarioAdminAtivo.setAtivo(Boolean.TRUE);
		usuarioAdminAtivo.setPerfil(perfilAdmin);

		when(usuarioRepository.findAll()).thenReturn(List.of(usuarioAdminAtivo, usuarioOperadorAtivo, usuarioOperadorInativo));

		List<UsuarioListagemResponse> response = listUsersService.listar(usuarioAutenticado);

		assertEquals(3, response.size());
		assertEquals(new UsuarioListagemResponse(1, "Administrador", "admin@email.com", 1, "ADMIN", Boolean.TRUE),
				response.getFirst());
		assertEquals(new UsuarioListagemResponse(3, "Carlos Lima", "carlos@email.com", 2, "OPERADOR", Boolean.FALSE),
				response.get(2));
		verify(usuarioRepository).findAll();
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverUsuariosCadastrados() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(usuarioRepository.findAll()).thenReturn(List.of());

		List<UsuarioListagemResponse> response = listUsersService.listar(usuarioAutenticado);

		assertEquals(List.of(), response);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listUsersService.listar(usuarioAutenticado));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(3, "carlos@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listUsersService.listar(usuarioAutenticado));
	}
}
