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

    @Autowired
    private com.tattooflow.modules.usuario.UsuarioRepository usuarios;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder encoder;

    private Convite convite(String codigo, boolean usado, java.time.LocalDateTime expiracao) {
        return conviteRepository.saveAndFlush(Convite.builder().codigo(codigo)
                .emailConvidado(codigo + "@teste.local").utilizado(usado).dataExpiracao(expiracao).build());
    }

    private ResultActions aceitar(String codigo, String pin) throws Exception {
        return mockMvc.perform(post("/api/convites/" + codigo + "/aceitar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Artista\",\"pin\":\"" + pin + "\",\"aparelhoId\":\"aparelho-convite\"}"));
    }

    @Test
    void aceitarDeveCriarTatuadorComHashEImpedirReuso() throws Exception {
        Convite convite = convite("valido", false, java.time.LocalDateTime.now().plusDays(1));
        aceitar("valido", "012345").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.perfil").value("TATUADOR"))
                .andExpect(jsonPath("$.data.senha").doesNotExist());
        var usuario = usuarios.findByEmail("valido@teste.local").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("012345", usuario.getSenha()));
        org.junit.jupiter.api.Assertions.assertTrue(conviteRepository.findById(convite.getId()).orElseThrow().getUtilizado());
        aceitar("valido", "012345").andExpect(status().isNotFound());
    }

    @Test
    void expiradoNaoDeveCriarUsuario() throws Exception {
        convite("expirado", false, java.time.LocalDateTime.now().minusSeconds(1));
        long antes = usuarios.count();
        aceitar("expirado", "123456").andExpect(status().isNotFound());
        assertEquals(antes, usuarios.count());
    }

    @Test
    void usadoNaoDeveSerAceito() throws Exception {
        convite("usado", true, java.time.LocalDateTime.now().plusDays(1));
        aceitar("usado", "123456").andExpect(status().isNotFound());
    }

    @Test
    void pinInvalidoNaoDeveConsumirConvite() throws Exception {
        Convite convite = convite("pin-invalido", false, java.time.LocalDateTime.now().plusDays(1));
        aceitar("pin-invalido", "abc").andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertFalse(conviteRepository.findById(convite.getId()).orElseThrow().getUtilizado());
    }

    @Test
    void emailJaCadastradoNaoDeveConsumirConvite() throws Exception {
        Convite convite = convite("duplicado", false, java.time.LocalDateTime.now().plusDays(1));
        usuarios.saveAndFlush(com.tattooflow.modules.usuario.Usuario.builder().nome("Existente")
                .email("duplicado@teste.local").senha(encoder.encode("123456"))
                .aparelhoId("outro-aparelho").perfil("TATUADOR").ativo(true).build());
        aceitar("duplicado", "123456").andExpect(status().isBadRequest());
        org.junit.jupiter.api.Assertions.assertFalse(conviteRepository.findById(convite.getId()).orElseThrow().getUtilizado());
    }

    @Test
    @WithMockUser(roles = "TATUADOR")
    void tatuadorNaoDeveListarConvites() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/convites"))
                .andExpect(status().isForbidden());
    }
}
