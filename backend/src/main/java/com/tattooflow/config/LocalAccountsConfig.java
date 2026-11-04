package com.tattooflow.config;

import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import com.tattooflow.modules.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
@ConditionalOnProperty(name = "app.local-accounts.enabled", havingValue = "true")
public class LocalAccountsConfig {
    @Bean
    CommandLineRunner criarContasLocais(UsuarioService usuarios, UsuarioRepository repository,
            @Value("${TATTOOFLOW_ADMIN_PIN}") String adminPin,
            @Value("${TATTOOFLOW_TATUADOR_PIN}") String tatuadorPin) {
        return args -> {
            criarSeAusente(usuarios, repository, "Administrador TattooFlow", "admin@tattooflow.local",
                    "ADMIN", "tattooflow-admin-local", adminPin);
            criarSeAusente(usuarios, repository, "Tatuador TattooFlow", "tatuador@tattooflow.local",
                    "TATUADOR", "tattooflow-tatuador-local", tatuadorPin);
        };
    }

    private void criarSeAusente(UsuarioService usuarios, UsuarioRepository repository, String nome,
            String email, String perfil, String aparelhoId, String pin) {
        if (repository.findByEmail(email).isEmpty()) {
            usuarios.salvar(Usuario.builder().nome(nome).email(email).perfil(perfil)
                    .aparelhoId(aparelhoId).senha(pin).ativo(true).build());
        }
    }
}
