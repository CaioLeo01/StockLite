package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

	@InjectMocks
	private InactivateUserService inactivateUserService;

	private Usuario usuarioAtivo;

	@BeforeEach
	void setUp() {
		Perfil perfil = new Perfil();
		perfil.setIdPerfil(2);
		perfil.setNome("VISUALIZADOR");

		usuarioAtivo = new Usuario();
		usuarioAtivo.setIdUsuario(2);
		usuarioAtivo.setNome("Maria");
		usuarioAtivo.setEmail("maria@email.com");
		usuarioAtivo.setAtivo(Boolean.TRUE);
		usuarioAtivo.setPerfil(perfil);
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
		verify(usuarioRepository, never()).findById(2);
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> inactivateUserService.inativar(999, usuarioAutenticado));
	}
}
