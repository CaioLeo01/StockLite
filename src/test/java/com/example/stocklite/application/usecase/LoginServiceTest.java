package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.LoginRequest;
import com.example.stocklite.application.dto.LoginResponse;
import com.example.stocklite.application.exception.InvalidCredentialsException;
import com.example.stocklite.application.exception.UserAccessDeniedException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.application.port.TokenService;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordHasher passwordHasher;

	@Mock
	private TokenService tokenService;

	@InjectMocks
	private LoginService loginService;

	private Usuario usuarioAtivo;

	@BeforeEach
	void setUp() {
		Perfil perfil = new Perfil();
		perfil.setIdPerfil(1);
		perfil.setNome("ADMIN");

		usuarioAtivo = new Usuario();
		usuarioAtivo.setIdUsuario(1);
		usuarioAtivo.setNome("Joao da Silva");
		usuarioAtivo.setEmail("joao@email.com");
		usuarioAtivo.setSenha("hash-gerado");
		usuarioAtivo.setAtivo(Boolean.TRUE);
		usuarioAtivo.setPerfil(perfil);
	}

	@Test
	void deveAutenticarUsuarioComSucesso() {
		LoginRequest request = new LoginRequest("  JOAO@EMAIL.COM  ", "SenhaForte@123");

		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.of(usuarioAtivo));
		when(passwordHasher.matches("SenhaForte@123", "hash-gerado")).thenReturn(true);
		when(tokenService.generateToken(usuarioAtivo)).thenReturn("jwt-gerado");

		LoginResponse response = loginService.autenticar(request);

		verify(usuarioRepository).findByEmailIgnoreCase("joao@email.com");
		assertEquals("Bearer jwt-gerado", response.token());
	}

	@Test
	void deveRetornarUnauthorizedQuandoUsuarioNaoExistir() {
		LoginRequest request = new LoginRequest("joao@email.com", "SenhaForte@123");

		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.empty());

		assertThrows(InvalidCredentialsException.class, () -> loginService.autenticar(request));
	}

	@Test
	void deveRetornarUnauthorizedQuandoSenhaForInvalida() {
		LoginRequest request = new LoginRequest("joao@email.com", "SenhaErrada@123");

		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.of(usuarioAtivo));
		when(passwordHasher.matches("SenhaErrada@123", "hash-gerado")).thenReturn(false);

		assertThrows(InvalidCredentialsException.class, () -> loginService.autenticar(request));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioEstiverInativo() {
		LoginRequest request = new LoginRequest("joao@email.com", "SenhaForte@123");
		usuarioAtivo.setAtivo(Boolean.FALSE);

		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.of(usuarioAtivo));
		when(passwordHasher.matches("SenhaForte@123", "hash-gerado")).thenReturn(true);

		assertThrows(UserAccessDeniedException.class, () -> loginService.autenticar(request));
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioNaoTiverPerfilValido() {
		LoginRequest request = new LoginRequest("joao@email.com", "SenhaForte@123");
		usuarioAtivo.getPerfil().setNome(" ");

		when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.of(usuarioAtivo));
		when(passwordHasher.matches("SenhaForte@123", "hash-gerado")).thenReturn(true);

		assertThrows(UserAccessDeniedException.class, () -> loginService.autenticar(request));
	}
}
