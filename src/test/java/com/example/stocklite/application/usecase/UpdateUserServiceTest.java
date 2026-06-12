package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.AtualizarUsuarioRequest;
import com.example.stocklite.application.dto.MessageResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.exception.ProfileNotFoundException;
import com.example.stocklite.application.exception.SelfUserUpdateNotAllowedException;
import com.example.stocklite.application.exception.UserNotFoundException;
import com.example.stocklite.application.exception.UserUpdateConflictException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PerfilRepository perfilRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private UpdateUserService updateUserService;

	private Perfil perfilAdmin;
	private Perfil perfilOperador;
	private Usuario usuarioAdminAtivo;
	private Usuario usuarioAdminInativo;
	private Usuario usuarioAlvo;

	@BeforeEach
	void setUp() {
		perfilAdmin = new Perfil();
		perfilAdmin.setIdPerfil(1);
		perfilAdmin.setNome("ADMIN");

		perfilOperador = new Perfil();
		perfilOperador.setIdPerfil(2);
		perfilOperador.setNome("OPERADOR");

		usuarioAdminAtivo = new Usuario();
		usuarioAdminAtivo.setIdUsuario(1);
		usuarioAdminAtivo.setNome("Admin");
		usuarioAdminAtivo.setEmail("admin@email.com");
		usuarioAdminAtivo.setAtivo(Boolean.TRUE);
		usuarioAdminAtivo.setPerfil(perfilAdmin);
		usuarioAdminAtivo.setSenha("hash-admin");

		usuarioAdminInativo = new Usuario();
		usuarioAdminInativo.setIdUsuario(1);
		usuarioAdminInativo.setNome("Admin");
		usuarioAdminInativo.setEmail("admin@email.com");
		usuarioAdminInativo.setAtivo(Boolean.FALSE);
		usuarioAdminInativo.setPerfil(perfilAdmin);
		usuarioAdminInativo.setSenha("hash-admin");

		usuarioAlvo = new Usuario();
		usuarioAlvo.setIdUsuario(2);
		usuarioAlvo.setNome("Maria");
		usuarioAlvo.setEmail("maria@email.com");
		usuarioAlvo.setAtivo(Boolean.TRUE);
		usuarioAlvo.setPerfil(perfilAdmin);
		usuarioAlvo.setSenha("senha-antiga");
	}

	@Test
	void deveAtualizarUsuarioComSucesso() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
				"Maria Silva",
				"  MARIA.SILVA@EMAIL.COM  ",
				2,
				Boolean.FALSE);

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAlvo));
		when(perfilRepository.findById(2)).thenReturn(Optional.of(perfilOperador));
		when(usuarioRepository.findByEmailIgnoreCase("maria.silva@email.com")).thenReturn(Optional.empty());
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MessageResponse response = updateUserService.atualizar(2, request, usuarioAutenticado);

		ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(usuarioCaptor.capture());

		Usuario usuarioSalvo = usuarioCaptor.getValue();
		assertEquals("Maria Silva", usuarioSalvo.getNome());
		assertEquals("maria.silva@email.com", usuarioSalvo.getEmail());
		assertEquals(perfilOperador, usuarioSalvo.getPerfil());
		assertFalse(usuarioSalvo.getAtivo());
		assertEquals("senha-antiga", usuarioSalvo.getSenha());

		assertEquals("Usuario atualizado com sucesso.", response.mensagem());
	}

	@Test
	void devePermitirManterMesmoEmailDoUsuarioAlvo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
				"Maria Souza",
				"  MARIA@EMAIL.COM ",
				2,
				Boolean.TRUE);

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAlvo));
		when(perfilRepository.findById(2)).thenReturn(Optional.of(perfilOperador));
		when(usuarioRepository.findByEmailIgnoreCase("maria@email.com")).thenReturn(Optional.of(usuarioAlvo));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MessageResponse response = updateUserService.atualizar(2, request, usuarioAutenticado);

		assertEquals("Usuario atualizado com sucesso.", response.mensagem());
		verify(usuarioRepository).save(usuarioAlvo);
	}

	@Test
	void deveRetornarConflictQuandoEmailPertencerAOutroUsuario() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
				"Maria Souza",
				"joao@email.com",
				2,
				Boolean.TRUE);
		Usuario usuarioComMesmoEmail = new Usuario();
		usuarioComMesmoEmail.setIdUsuario(99);
		usuarioComMesmoEmail.setEmail("joao@email.com");

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAlvo));
		when(perfilRepository.findById(2)).thenReturn(Optional.of(perfilOperador));
		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.of(usuarioComMesmoEmail));

		assertThrows(UserUpdateConflictException.class,
				() -> updateUserService.atualizar(2, request, usuarioAutenticado));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com", 2, Boolean.TRUE);

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> updateUserService.atualizar(2, request, usuarioAutenticado));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com", 2, Boolean.TRUE);

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> updateUserService.atualizar(2, request, usuarioAutenticado));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void deveRetornarForbiddenQuandoAdministradorTentarAtualizarASiMesmo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(2, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com", 2, Boolean.TRUE);

		assertThrows(SelfUserUpdateNotAllowedException.class,
				() -> updateUserService.atualizar(2, request, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com", 2, Boolean.TRUE);

		when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class,
				() -> updateUserService.atualizar(999, request, usuarioAutenticado));
	}

	@Test
	void deveRetornarNotFoundQuandoPerfilNaoExistir() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com", 99, Boolean.TRUE);

		when(usuarioRepository.findById(2)).thenReturn(Optional.of(usuarioAlvo));
		when(perfilRepository.findById(99)).thenReturn(Optional.empty());

		assertThrows(ProfileNotFoundException.class,
				() -> updateUserService.atualizar(2, request, usuarioAutenticado));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}
}
