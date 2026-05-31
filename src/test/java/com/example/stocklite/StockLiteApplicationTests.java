package com.example.stocklite;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.stocklite.application.port.PasswordHasher;
import com.example.stocklite.domain.repository.PerfilRepository;
import com.example.stocklite.domain.repository.UsuarioRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataPerfilRepository;
import com.example.stocklite.infrastructure.persistence.repository.SpringDataUsuarioRepository;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.orm.jpa.autoconfigure.HibernateJpaAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
class StockLiteApplicationTests {

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
	void contextLoads() {
	}

}
