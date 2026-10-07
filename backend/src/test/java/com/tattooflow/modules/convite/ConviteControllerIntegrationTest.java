package com.tattooflow.modules.convite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:convite_security_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ConviteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConviteRepository conviteRepository;

    private static final String CONVITE_JSON = """
            {"emailConvidado":"artista@tattooflow.com"}
            """;

    @Test
    @WithMockUser(roles = "ADMIN")
    void administradorDeveCriarConvite() throws Exception {
        long totalAntes = conviteRepository.count();

        mockMvc.perform(post("/api/convites")
                        .contentType(MediaType.APPLICATION_JSON).content(CONVITE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.emailConvidado").value("artista@tattooflow.com"))
                .andExpect(jsonPath("$.data.codigo").isNotEmpty());

        assertEquals(totalAntes + 1, conviteRepository.count());
    }

    @Test
    @WithMockUser(roles = "TATUADOR")
    void tatuadorNaoDeveCriarConvite() throws Exception {
        verificarCriacaoBloqueada()
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Somente administradores podem enviar convites"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void criacaoDeveExigirAutenticacao() throws Exception {
        verificarCriacaoBloqueada();
    }

    private ResultActions verificarCriacaoBloqueada() throws Exception {
        long totalAntes = conviteRepository.count();

        ResultActions resultado = mockMvc.perform(post("/api/convites")
                        .contentType(MediaType.APPLICATION_JSON).content(CONVITE_JSON))
                .andExpect(status().isForbidden());

        assertEquals(totalAntes, conviteRepository.count());
        return resultado;
    }
}
