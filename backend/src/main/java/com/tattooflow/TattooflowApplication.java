package com.tattooflow;

import com.tattooflow.service.InstagramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class TattooflowApplication {

    public static void main(String[] args) {
        carregarVariaveisEnvSeExistir();
        SpringApplication.run(TattooflowApplication.class, args);
    }

    /**
     * Carrega variáveis do arquivo .env caso ainda não estejam definidas no ambiente.
     */
    private static void carregarVariaveisEnvSeExistir() {
        Path[] possiveisCaminhos = new Path[]{
                Paths.get(".env"),
                Paths.get("backend", ".env")
        };

        for (Path caminho : possiveisCaminhos) {
            if (Files.exists(caminho)) {
                try {
                    List<String> linhas = Files.readAllLines(caminho);
                    for (String linha : linhas) {
                        linha = linha.trim();
                        if (linha.isEmpty() || linha.startsWith("#")) {
                            continue;
                        }
                        int sep = linha.indexOf('=');
                        if (sep > 0) {
                            String chave = linha.substring(0, sep).trim();
                            String valor = linha.substring(sep + 1).trim();
                            if (System.getProperty(chave) == null && System.getenv(chave) == null) {
                                System.setProperty(chave, valor);
                            }
                        }
                    }
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * CommandLineRunner temporário para validar a comunicação com Instagram Graph API logo após o Spring Boot subir.
     */
    @Bean
    @Profile({"!test", "!h2"})
    public CommandLineRunner testarInstagramConexaoTemporario(@Autowired(required = false) InstagramService instagramService) {
        return args -> {
            if (instagramService == null) {
                return;
            }
            System.out.println("\n==================================================================");
            System.out.println(">>> [INSTAGRAM-GRAPH-API] INICIANDO TESTE TEMPORARIO DE CONEXAO <<<");
            System.out.println("==================================================================");
            try {
                instagramService.testarConexao();
            } catch (Exception e) {
                System.err.println(">>> [INSTAGRAM-GRAPH-API] Falha no teste: " + e.getMessage());
            }
            System.out.println("==================================================================\n");
        };
    }
}
