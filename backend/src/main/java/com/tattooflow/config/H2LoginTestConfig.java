package com.tattooflow.config;

import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("h2")
@ConditionalOnProperty(name = "app.login-test.enabled", havingValue = "true")
public class H2LoginTestConfig {
    @Bean
    CommandLineRunner criarUsuarioTeste(UsuarioService usuarioService,
            @Value("${app.login-test.pin}") String pin) {
        return args -> usuarioService.salvar(Usuario.builder()
                .nome("Administrador de teste")
                .email("admin@teste.local")
                .senha(pin)
                .aparelhoId("aparelho-admin-teste")
                .perfil("ADMIN")
                .ativo(true)
                .build());
    }
}
