package com.tattooflow.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.usuario.CriarUsuarioRequest;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import com.tattooflow.modules.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:agendamento_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class AuthControllerIntegrationTest {

    private static final String PIN = "012345";
    private static final String APARELHO = "aparelho-login-test";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private Usuario usuario;

    @BeforeEach
    void prepararUsuario() {
        usuario = usuarioService.salvar(Usuario.builder()
                .nome("Tatuador do login")
                .email("login@tattooflow.com")
                .senha(PIN)
                .aparelhoId(APARELHO)
                .perfil("TATUADOR")
                .ativo(true)
                .build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/login", "/api/auth/login"})
    void deveAutenticarERetornarJwtQueAcessaAgendamentos(String rota) throws Exception {
        MvcResult resultado = mockMvc.perform(post(rota)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(PIN, APARELHO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString())
                .andReturn();

        String token = objectMapper.readTree(resultado.getResponse().getContentAsString()).get("data").asText();
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(usuario.getEmail(), jwtUtil.getUsernameFromToken(token));

        mockMvc.perform(get("/api/agendamentos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deveAutenticarAdministrador() throws Exception {
        usuario.setPerfil("ADMIN");
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, APARELHO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString());
    }

    @Test
    void deveRejeitarPinIncorretoSemRetornarToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson("999999", APARELHO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("PIN inválido"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deveRejeitarAparelhoNaoAutorizadoSemRetornarToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, "aparelho-desconhecido")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Aparelho não autorizado"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deveVerificarPinDoDonoDoAparelhoInformado() throws Exception {
        usuarioService.salvar(Usuario.builder()
                .nome("Outro tatuador").email("outro@tattooflow.com")
                .senha("654321").aparelhoId("outro-aparelho").perfil("TATUADOR").ativo(true).build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, "outro-aparelho")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("PIN inválido"));
    }

    @Test
    void deveRejeitarUsuarioInativo() throws Exception {
        usuario.setAtivo(false);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, APARELHO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Usuário inativo"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deveRejeitarPerfilSemAcesso() throws Exception {
        usuario.setPerfil("CLIENTE");
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, APARELHO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Usuário não autorizado"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123", "1234567", "abcd", "12 34", "012345 "})
    void deveRejeitarFormatoInvalidoDoPin(String pin) throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(pin, APARELHO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void deveExigirIdDoAparelho(String aparelhoId) throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, aparelhoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID do aparelho é obrigatório"));
    }

    @Test
    void deveRejeitarIdDoAparelhoAcimaDoLimite() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, "a".repeat(256))))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "{\"pin\":\"012345\"}", "{\"aparelhoId\":\"aparelho-login-test\"}"})
    void deveRejeitarJsonInvalidoOuCamposAusentes(String json) throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deveRejeitarCorpoAusente() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginAntigoPorEmailESenhaNaoDeveGerarToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "qualquer@tattooflow.com").param("senha", "qualquer"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void naoDeveAceitarPinLegadoEmTextoPuro() throws Exception {
        usuario.setSenha(PIN);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, APARELHO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirNovoLoginMesmoComJwtDeUsuarioRemovido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken("removido@tattooflow.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, APARELHO)))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearJwtAposDesativarUsuario() throws Exception {
        String token = jwtUtil.generateToken(usuario.getEmail());
        usuario.setAtivo(false);
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(get("/api/agendamentos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearJwtDeUsuarioRemovidoSemErroInterno() throws Exception {
        String token = jwtUtil.generateToken(usuario.getEmail());
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();

        mockMvc.perform(get("/api/agendamentos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDeveCadastrarAparelhoEPinComHashSemExporCredencial() throws Exception {
        usuario.setPerfil("ADMIN");
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(usuario.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioJson("novo-aparelho")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aparelhoId").value("novo-aparelho"))
                .andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.data.pin").doesNotExist());

        Usuario cadastrado = usuarioRepository.findByAparelhoId("novo-aparelho").orElseThrow();
        assertNotEquals(PIN, cadastrado.getSenha());
        assertTrue(cadastrado.getSenha().startsWith("$2a$12$"));
        assertTrue(passwordEncoder.matches(PIN, cadastrado.getSenha()));

        mockMvc.perform(get("/api/usuarios/" + cadastrado.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(usuario.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.senha").doesNotExist());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(loginJson(PIN, "novo-aparelho")))
                .andExpect(status().isOk());
    }

    @Test
    void tatuadorNaoDeveAutorizarNovoAparelho() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(usuario.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioJson("novo-aparelho")))
                .andExpect(status().isForbidden());

        assertTrue(usuarioRepository.findByAparelhoId("novo-aparelho").isEmpty());
    }

    @Test
    void cadastroDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioJson("novo-aparelho")))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveVincularMesmoAparelhoADoisUsuarios() throws Exception {
        usuario.setPerfil("ADMIN");
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtil.generateToken(usuario.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON).content(novoUsuarioJson(APARELHO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Aparelho já vinculado a outro usuário"));
    }

    @Test
    void pinNaoDeveAparecerNaRepresentacaoDosRequests() {
        assertFalse(new LoginRequest(PIN, APARELHO).toString().contains(PIN));
        assertFalse(new CriarUsuarioRequest("Nome", "teste@tattooflow.com", PIN, APARELHO, "ADMIN")
                .toString().contains(PIN));
    }

    private String loginJson(String pin, String aparelhoId) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequest(pin, aparelhoId));
    }

    private String novoUsuarioJson(String aparelhoId) {
        return """
                {"nome":"Novo tatuador","email":"novo@tattooflow.com","pin":"012345",
                 "aparelhoId":"%s","perfil":"TATUADOR"}
                """.formatted(aparelhoId);
    }
}
