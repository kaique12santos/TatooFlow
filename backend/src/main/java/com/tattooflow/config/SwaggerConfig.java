package com.tattooflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TattooFlow API")
                        .version("1.0.0")
                        .description("Documentação dos endpoints do sistema TattooFlow")
                        .contact(new Contact().name("Equipe TattooFlow")));
    }
}
