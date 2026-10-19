package com.tattooflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole apenas o JWT retornado no campo data de POST /auth/login (PIN + aparelhoId), sem o prefixo Bearer.")))
                .info(new Info()
                        .title("TattooFlow API")
                        .version("1.0.0")
                        .description("Documentação dos endpoints do sistema TattooFlow")
                        .contact(new Contact().name("Equipe TattooFlow")));
    }
}
