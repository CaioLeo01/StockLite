package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.RegisterRequest;
import com.example.stocklite.application.dto.RegisterResponse;
import com.example.stocklite.application.exception.EmailAlreadyInUseException;
import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.model.Usuario;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PerfilRepository perfilRepository;

	@Mock
	private PasswordHasher passwordHasher;

	@InjectMocks
	private RegisterUserService registerUserService;

	private Perfil perfilVisualizador;

	@BeforeEach
	void setUp() {
		perfilVisualizador = new Perfil();
		perfilVisualizador.setIdPerfil(3);
		perfilVisualizador.setNome("VISUALIZADOR");
	}

	@Test
	void deveRegistrarUsuarioComEmailNormalizadoESenhaComHash() {
		RegisterRequest request = new RegisterRequest(
				"Joao da Silva",
				"  JOAO@EMAIL.COM  ",
				"SenhaForte@123");

		when(usuarioRepository.existsByEmailIgnoreCase("joao@email.com")).thenReturn(false);
		when(perfilRepository.findByNomeIgnoreCase("VISUALIZADOR")).thenReturn(Optional.of(perfilVisualizador));
		when(passwordHasher.hash("SenhaForte@123")).thenReturn("hash-gerado");
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario usuario = invocation.getArgument(0);
			usuario.setIdUsuario(1);
			usuario.setDataCriacao(OffsetDateTime.parse("2026-05-28T10:30:00-03:00"));
			return usuario;
		});

		RegisterResponse response = registerUserService.registrar(request);

		ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(usuarioCaptor.capture());

		Usuario usuarioSalvo = usuarioCaptor.getValue();
		assertEquals("Joao da Silva", usuarioSalvo.getNome());
		assertEquals("joao@email.com", usuarioSalvo.getEmail());
		assertEquals("hash-gerado", usuarioSalvo.getSenha());
		assertTrue(usuarioSalvo.getAtivo());
		assertEquals(perfilVisualizador, usuarioSalvo.getPerfil());

		assertEquals(1, response.idUsuario());
		assertEquals("joao@email.com", response.email());
		assertEquals("Usuario registrado com sucesso.", response.mensagem());
	}

	@Test
	void deveRejeitarCadastroQuandoEmailJaExistir() {
		RegisterRequest request = new RegisterRequest(
				"Joao da Silva",
				"joao@email.com",
				"SenhaForte@123");

		when(usuarioRepository.existsByEmailIgnoreCase("joao@email.com")).thenReturn(true);

		assertThrows(EmailAlreadyInUseException.class, () -> registerUserService.registrar(request));
	}
}
