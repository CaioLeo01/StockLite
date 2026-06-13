package com.example.stocklite.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.stocklite.application.dto.PerfilResponse;
import com.example.stocklite.application.exception.AuthenticatedUserInactiveOrNotFoundException;
import com.example.stocklite.application.security.AuthenticatedUser;
import com.example.stocklite.domain.model.Perfil;
import com.example.stocklite.domain.repository.PerfilRepository;

@ExtendWith(MockitoExtension.class)
class ListarPerfisServiceTest {

	@Mock
	private PerfilRepository perfilRepository;

	@Mock
	private AuthenticatedUserValidator authenticatedUserValidator;

	@InjectMocks
	private ListarPerfisService listarPerfisService;

	@Test
	void deveListarPerfisQuandoUsuarioAutenticadoForValidoEAtivo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");
		Perfil perfilAdmin = criarPerfil(1, "ADMIN", "Perfil com acesso administrativo ao sistema.");
		Perfil perfilEstoquista = criarPerfil(2, "ESTOQUISTA",
				"Perfil responsavel por cadastrar produtos e realizar movimentacoes de estoque.");

		when(perfilRepository.findAll()).thenReturn(List.of(perfilAdmin, perfilEstoquista));

		List<PerfilResponse> response = listarPerfisService.listar(usuarioAutenticado);

		assertEquals(2, response.size());
		assertEquals(new PerfilResponse(1, "ADMIN", "Perfil com acesso administrativo ao sistema."),
				response.getFirst());
		assertEquals(new PerfilResponse(2, "ESTOQUISTA",
				"Perfil responsavel por cadastrar produtos e realizar movimentacoes de estoque."),
				response.get(1));
		verify(perfilRepository).findAll();
		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, null);
	}

	@Test
	void deveRetornarListaVaziaQuandoNaoHouverPerfisCadastrados() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(1, "admin@email.com", "ADMIN");

		when(perfilRepository.findAll()).thenReturn(List.of());

		List<PerfilResponse> response = listarPerfisService.listar(usuarioAutenticado);

		assertEquals(List.of(), response);
		verify(authenticatedUserValidator).validarUsuarioAtivo(usuarioAutenticado, null);
	}

	@Test
	void deveRetornarForbiddenQuandoUsuarioAutenticadoNaoExistirOuEstiverInativo() {
		AuthenticatedUser usuarioAutenticado = new AuthenticatedUser(999, "admin@email.com", "ADMIN");

		doThrow(new AuthenticatedUserInactiveOrNotFoundException())
				.when(authenticatedUserValidator)
				.validarUsuarioAtivo(usuarioAutenticado, null);

		assertThrows(AuthenticatedUserInactiveOrNotFoundException.class,
				() -> listarPerfisService.listar(usuarioAutenticado));
	}

	private Perfil criarPerfil(Integer idPerfil, String nome, String descricao) {
		Perfil perfil = new Perfil();
		perfil.setIdPerfil(idPerfil);
		perfil.setNome(nome);
		perfil.setDescricao(descricao);
		return perfil;
	}
}
