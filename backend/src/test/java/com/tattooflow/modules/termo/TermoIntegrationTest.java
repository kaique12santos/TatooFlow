package com.tattooflow.modules.termo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:termo_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
@WithMockUser(roles = "TATUADOR")
class TermoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TermoRepository termoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    private Cliente cliente;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(Cliente.builder()
                .nome("Mariana Costa")
                .telefone("11988887777")
                .email("mariana@exemplo.com")
                .build());

        agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(cliente.getId())
                .tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 20, 15, 0))
                .duracaoMinutos(120)
                .status("CONFIRMADO")
                .valorEstimado(new BigDecimal("400.00"))
                .build());
    }

    @Test
    @DisplayName("[F3-BANCO-01] Deve registrar termo de aceite e persistir no banco com retenção mínima de 5 anos (RN06)")
    void deveRegistrarTermoComRetencaoMinimaNoBanco() throws Exception {
        TermoAceite payload = TermoAceite.builder()
                .clienteId(cliente.getId())
                .agendamentoId(agendamento.getId())
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-mariana.pdf")
                .ipAssinatura("177.18.25.10")
                .dataAssinatura(LocalDateTime.of(2026, 10, 27, 10, 30))
                .build();

        mockMvc.perform(post("/api/termos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.data.agendamentoId").value(agendamento.getId()))
                .andExpect(jsonPath("$.data.dataExpiracaoRetencao").exists());

        TermoAceite persistido = termoRepository.findAll().stream()
                .filter(t -> t.getClienteId().equals(cliente.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(persistido.getUrlPdfAssinado()).isEqualTo("https://storage.exemplo.com/termos/termo-mariana.pdf");
        assertThat(persistido.getIpAssinatura()).isEqualTo("177.18.25.10");
        assertThat(persistido.getDataAssinatura()).isEqualTo(LocalDateTime.of(2026, 10, 27, 10, 30));
        // RN06: dataExpiracaoRetencao deve ser exatamente dataAssinatura + 5 anos
        assertThat(persistido.getDataExpiracaoRetencao()).isEqualTo(LocalDateTime.of(2031, 10, 27, 10, 30));
    }

    @Test
    @DisplayName("[F3-BANCO-01] Deve rejeitar registro de termo com retenção menor que 5 anos (RN06)")
    void deveRejeitarRegistroComRetencaoMenorQue5Anos() throws Exception {
        LocalDateTime assinatura = LocalDateTime.of(2026, 10, 27, 10, 0);
        LocalDateTime retencaoInvalida = assinatura.plusYears(3);

        TermoAceite payload = TermoAceite.builder()
                .clienteId(cliente.getId())
                .agendamentoId(agendamento.getId())
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-invalido.pdf")
                .dataAssinatura(assinatura)
                .dataExpiracaoRetencao(retencaoInvalida)
                .build();

        mockMvc.perform(post("/api/termos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("RN06")));
    }

    @Test
    @DisplayName("Deve buscar termo por id via endpoint")
    void deveBuscarTermoPorId() throws Exception {
        TermoAceite salvo = termoRepository.save(TermoAceite.builder()
                .clienteId(cliente.getId())
                .agendamentoId(agendamento.getId())
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-busca.pdf")
                .dataAssinatura(LocalDateTime.now())
                .dataExpiracaoRetencao(LocalDateTime.now().plusYears(5))
                .build());

        mockMvc.perform(get("/api/termos/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(salvo.getId()))
                .andExpect(jsonPath("$.data.urlPdfAssinado").value(salvo.getUrlPdfAssinado()));
    }
}
