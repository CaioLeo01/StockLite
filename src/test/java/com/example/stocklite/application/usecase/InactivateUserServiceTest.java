package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.SelfUserDeletionNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class InactivateUserServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private InactivateUserService inactivateUserService;

	private Usuario usuarioAtivo;
	private Usuario usuarioAdminAtivo;
	private Usuario usuarioAdminInativo;

	@BeforeEach
	void setUp() {
		Perfil perfil = new Perfil();
		perfil.setIdPerfil(2);
		perfil.setNome("VISUALIZADOR");

		Perfil perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

		usuarioAtivo = new Usuario();
		usuarioAtivo.setIdUsuario(2);
		usuarioAtivo.setNome("Maria");
		usuarioAtivo.setEmail("maria@email.com");
		usuarioAtivo.setAtivo(Boolean.TRUE);
		usuarioAtivo.setPerfil(perfil);

		usuarioAdminAtivo = new Usuario();
		usuarioAdminAtivo.setIdUsuario(1);
		usuarioAdminAtivo.setNome("Admin");
		usuarioAdminAtivo.setEmail("admin@email.com");
		usuarioAdminAtivo.setAtivo(Boolean.TRUE);
		usuarioAdminAtivo.setPerfil(perfilAdmin);

		usuarioAdminInativo = new Usuario();
		usuarioAdminInativo.setIdUsuario(1);
		usuarioAdminInativo.setNome("Admin");
		usuarioAdminInativo.setEmail("admin@email.com");
		usuarioAdminInativo.setAtivo(Boolean.FALSE);
		usuarioAdminInativo.setPerfil(perfilAdmin);
	}

	@Test
	void deveInativarUsuarioComSucessoQuandoAutenticadoForAdmin() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAtivo));
		when(usuarioRepository.save(usuarioAtivo)).thenReturn(usuarioAtivo);

		InactivateUserResult response = inactivateUserService.inativar(2, usuarioAutenticado);

		assertEquals(InactivateUserResult.INATIVADO, response);
		assertEquals(Boolean.FALSE, usuarioAtivo.getAtivo());
		verify(usuarioRepository).save(usuarioAtivo);
	}

	@Test
	void deveRetornarMensagemInformativaQuandoUsuarioJaEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		usuarioAtivo.setAtivo(Boolean.FALSE);

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAtivo));

		InactivateUserResult response = inactivateUserService.inativar(2, usuarioAutenticado);

		assertEquals(InactivateUserResult.JA_ESTAVA_INATIVO, response);
		verify(usuarioRepository, never()).save(usuarioAtivo);
	}

	@Test
	void deveRetornarForbiddenQuandoAdministradorTentarInativarASiMesmo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "admin@email.com", "ADMIN");

		assertThrows(SelfUserDeletionNotAllowedException.class,
				() -> inactivateUserService.inativar(2, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> inactivateUserService.inativar(999, usuarioAutenticado));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> inactivateUserService.inativar(2, usuarioAutenticado));
		verify(usuarioRepository, never()).save(usuarioAtivo);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> inactivateUserService.inativar(2, usuarioAutenticado));
		verify(usuarioRepository, never()).save(usuarioAtivo);
	}
}
