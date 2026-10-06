package com.tattooflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:agendamento_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SwaggerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveDocumentarJwtNosAgendamentosComLoginEDocumentacaoPublicos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/agendamentos'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/agendamentos/tatuador/{tatuadorId}'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/agendamentos'].post.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/auth/login'].post.requestBody.content['application/json']").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/auth/login'].post.responses['403']").exists())
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.pin.writeOnly").value(true))
                .andExpect(jsonPath("$.components.schemas.LoginRequest.properties.aparelhoId").exists())
                .andExpect(jsonPath("$.components.schemas.Usuario.properties.senha").doesNotExist())
                .andExpect(jsonPath("$.security").doesNotExist());
    }
}
