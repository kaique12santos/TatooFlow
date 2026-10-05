package com.tattooflow.modules.agendamento;

import com.tattooflow.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
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

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:agendamento_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class AgendamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    private String authorization;

    @BeforeEach
    void prepararBanco() {
        repository.deleteAll();
        authorization = "Bearer " + jwtUtil.generateToken("agenda@tattooflow.com");
    }

    @Test
    void deveListarAgendamentosPersistidosNoBanco() throws Exception {
        Agendamento primeiro = salvarAgendamento(1L, "AGENDADO");
        Agendamento segundo = salvarAgendamento(2L, "CANCELADO");

        mockMvc.perform(get("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].id", hasItems(
                        primeiro.getId().intValue(), segundo.getId().intValue())))
                .andExpect(jsonPath("$.data[*].clienteId", hasItems(1, 2)))
                .andExpect(jsonPath("$.data[*].tatuadorId", hasItems(1, 2)))
                .andExpect(jsonPath("$.data[*].dataHora", hasItems("2026-10-10T12:00:00")))
                .andExpect(jsonPath("$.data[*].duracaoMinutos", hasItems(120)))
                .andExpect(jsonPath("$.data[*].descricaoSessao", hasItems("Fechamento de braco")))
                .andExpect(jsonPath("$.data[*].valorEstimado", hasItems(800.0)))
                .andExpect(jsonPath("$.data[*].status", hasItems("AGENDADO", "CANCELADO")));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistemAgendamentos() throws Exception {
        mockMvc.perform(get("/api/agendamentos")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void deveListarSomenteAgendamentosDoTatuador() throws Exception {
        Agendamento esperado = salvarAgendamento(1L, "AGENDADO");
        salvarAgendamento(2L, "AGENDADO");

        mockMvc.perform(get("/api/agendamentos/tatuador/1")
                        .header(HttpHeaders.AUTHORIZATION, authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(esperado.getId()))
                .andExpect(jsonPath("$.data[0].tatuadorId").value(1));
    }

    @Test
    void deveExigirAutenticacaoParaListarAgendamentos() throws Exception {
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isForbidden());
    }

    private Agendamento salvarAgendamento(Long tatuadorId, String status) {
        return repository.saveAndFlush(Agendamento.builder()
                .clienteId(tatuadorId)
                .tatuadorId(tatuadorId)
                .dataHora(LocalDateTime.of(2026, 10, 10, 12, 0))
                .duracaoMinutos(120)
                .descricaoSessao("Fechamento de braco")
                .valorEstimado(new BigDecimal("800.00"))
                .status(status)
                .build());
    }
}
