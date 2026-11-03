package com.tattooflow.modules.agendamento;

import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:agenda_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class AgendaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    private String authorization;

    @BeforeEach
    void prepararCenario() {
        repository.deleteAll();
        usuarioService.salvar(Usuario.builder()
                .nome("Tatuador Agenda")
                .email("tatuador.agenda@tattooflow.com")
                .senha("123456")
                .aparelhoId("aparelho-agenda-01")
                .perfil("TATUADOR")
                .ativo(true)
                .build());
        authorization = "Bearer " + jwtUtil.generateToken("tatuador.agenda@tattooflow.com");
    }

    private Agendamento criarAgendamento(Long tatuadorId, LocalDateTime dataHora, String status) {
        return repository.saveAndFlush(Agendamento.builder()
                .clienteId(10L)
                .tatuadorId(tatuadorId)
                .dataHora(dataHora)
                .duracaoMinutos(90)
                .descricaoSessao("Tatuagem Old School")
                .valorEstimado(new BigDecimal("500.00"))
                .status(status)
                .build());
    }

    @Test
    @DisplayName("GET /agenda?periodo=dia deve retornar apenas agendamentos do dia especificado")
    void deveListarAgendaPorDia() throws Exception {
        // Quarta-feira 2026-10-14
        Agendamento hoje = criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 14, 0), "AGENDADO");
        Agendamento amanha = criarAgendamento(1L, LocalDateTime.of(2026, 10, 15, 10, 0), "AGENDADO");

        mockMvc.perform(get("/agenda")
                        .param("periodo", "dia")
                        .param("data", "2026-10-14")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(hoje.getId()))
                .andExpect(jsonPath("$.data[0].dataHora").value("2026-10-14T14:00:00"));
    }

    @Test
    @DisplayName("GET /agenda?periodo=semana deve retornar agendamentos da semana de segunda a domingo")
    void deveListarAgendaPorSemana() throws Exception {
        // Semana: 2026-10-12 (Segunda) até 2026-10-18 (Domingo)
        Agendamento antes = criarAgendamento(1L, LocalDateTime.of(2026, 10, 11, 15, 0), "AGENDADO"); // Domingo anterior
        Agendamento seg = criarAgendamento(1L, LocalDateTime.of(2026, 10, 12, 9, 0), "AGENDADO");
        Agendamento qua = criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 16, 0), "CONFIRMADO");
        Agendamento dom = criarAgendamento(1L, LocalDateTime.of(2026, 10, 18, 20, 0), "AGENDADO");
        Agendamento depois = criarAgendamento(1L, LocalDateTime.of(2026, 10, 19, 10, 0), "AGENDADO"); // Segunda posterior

        mockMvc.perform(get("/agenda")
                        .param("periodo", "semana")
                        .param("data", "2026-10-14")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].id", contains(
                        seg.getId().intValue(),
                        qua.getId().intValue(),
                        dom.getId().intValue()
                )));
    }

    @Test
    @DisplayName("GET /agenda?periodo=mes deve retornar agendamentos do mês inteiro")
    void deveListarAgendaPorMes() throws Exception {
        Agendamento set = criarAgendamento(1L, LocalDateTime.of(2026, 9, 30, 23, 0), "AGENDADO");
        Agendamento out1 = criarAgendamento(1L, LocalDateTime.of(2026, 10, 1, 8, 0), "AGENDADO");
        Agendamento out2 = criarAgendamento(1L, LocalDateTime.of(2026, 10, 15, 14, 0), "EM_ANDAMENTO");
        Agendamento out3 = criarAgendamento(1L, LocalDateTime.of(2026, 10, 31, 22, 0), "CONCLUIDO");
        Agendamento nov = criarAgendamento(1L, LocalDateTime.of(2026, 11, 1, 8, 0), "AGENDADO");

        mockMvc.perform(get("/agenda")
                        .param("periodo", "mes")
                        .param("data", "2026-10-15")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].id", contains(
                        out1.getId().intValue(),
                        out2.getId().intValue(),
                        out3.getId().intValue()
                )));
    }

    @Test
    @DisplayName("GET /agenda com filtro por status deve retornar apenas o status solicitado")
    void deveFiltrarPorStatus() throws Exception {
        criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 10, 0), "AGENDADO");
        Agendamento confirmado = criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 14, 0), "CONFIRMADO");
        criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 18, 0), "CANCELADO");

        mockMvc.perform(get("/agenda")
                        .param("periodo", "dia")
                        .param("data", "2026-10-14")
                        .param("status", "CONFIRMADO")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(confirmado.getId()))
                .andExpect(jsonPath("$.data[0].status").value("CONFIRMADO"));
    }

    @Test
    @DisplayName("GET /api/agenda também deve funcionar (compatibilidade de rota)")
    void deveResponderNaRotaApiAgenda() throws Exception {
        Agendamento a = criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 11, 0), "AGENDADO");

        mockMvc.perform(get("/api/agenda")
                        .param("periodo", "dia")
                        .param("data", "2026-10-14")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(a.getId()));
    }

    @Test
    @DisplayName("GET /agenda com periodo=dia|semana|mes literal deve funcionar")
    void deveAceitarNotacaoLiteralDoCriterio() throws Exception {
        Agendamento a = criarAgendamento(1L, LocalDateTime.of(2026, 10, 14, 11, 0), "AGENDADO");

        mockMvc.perform(get("/agenda")
                        .param("periodo", "dia|semana|mes")
                        .param("data", "2026-10-14")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(a.getId()));
    }

    @Test
    @DisplayName("GET /agenda com período inválido deve retornar 400 Bad Request")
    void deveRetornarErroParaPeriodoInvalido() throws Exception {
        mockMvc.perform(get("/agenda")
                        .param("periodo", "ano")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Período inválido")));
    }

    @Test
    @DisplayName("GET /agenda com status inválido deve retornar 400 Bad Request")
    void deveRetornarErroParaStatusInvalido() throws Exception {
        mockMvc.perform(get("/agenda")
                        .param("status", "STATUS_INEXISTENTE")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Status de agendamento inválido")));
    }

    @Test
    @DisplayName("GET /agenda sem token deve retornar 403 Forbidden")
    void deveBloquearAcessoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/agenda"))
                .andExpect(status().isForbidden());
    }
}

