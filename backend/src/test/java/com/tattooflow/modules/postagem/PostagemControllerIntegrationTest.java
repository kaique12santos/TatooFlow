package com.tattooflow.modules.postagem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import com.tattooflow.service.GeminiService;
import com.tattooflow.service.ImagemService;
import com.tattooflow.service.InstagramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:postagem_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "instagram.scheduler.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
@WithMockUser(roles = "TATUADOR")
class PostagemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostagemAgendadaRepository postagemAgendadaRepository;

    @Autowired
    private PostagemLogRepository postagemLogRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstagramService instagramService;

    @MockBean
    private ImagemService imagemService;

    @MockBean
    private GeminiService geminiService;

    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nome("Marina Ruy")
                .telefone("11988887777")
                .email("marina@exemplo.com")
                .build());

        agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(cliente.getId())
                .tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 12, 10, 15, 0))
                .duracaoMinutos(120)
                .status("CONFIRMADO")
                .valorEstimado(new BigDecimal("800.00"))
                .build());

        when(instagramService.getAccountId()).thenReturn("17841416931904965");
        when(instagramService.publicarFoto(any(), any()))
                .thenReturn(PublicacaoResultado.sucesso("container_teste_123", "media_teste_456", "{\"id\":\"media_teste_456\"}"));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve agendar postagem via POST /midias/agendamento-postagem")
    void deveAgendarPostagemViaEndpointRest() throws Exception {
        AgendarPostagemRequest request = AgendarPostagemRequest.builder()
                .agendamentoId(agendamento.getId())
                .imageUrl("https://res.cloudinary.com/demo/image/upload/v1/tatoo.webp")
                .legenda("Estudo de São Jerônimo FineLine #TattooFlow https://wa.me/5511999999999")
                .dataHoraAgendada(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/midias/agendamento-postagem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.protocolo").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("AGENDADO"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://res.cloudinary.com/demo/image/upload/v1/tatoo.webp"));

        assertThat(postagemAgendadaRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve consultar logs de auditoria via GET /midias/agendamento-postagem/{id}/logs")
    void deveConsultarLogsDeAuditoria() throws Exception {
        PostagemAgendada postagem = postagemAgendadaRepository.save(PostagemAgendada.builder()
                .imageUrl("https://imagem.jpg")
                .legenda("Legenda de teste")
                .dataHoraAgendada(LocalDateTime.now().plusHours(1))
                .protocolo("TATTOO-MKT-LOGS-01")
                .status("AGENDADO")
                .build());

        postagemLogRepository.save(PostagemLog.builder()
                .postagemAgendadaId(postagem.getId())
                .tipoEvento("AGENDAMENTO_CRIADO")
                .mensagem("Postagem agendada")
                .sucesso(true)
                .dataHora(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/midias/agendamento-postagem/" + postagem.getId() + "/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tipoEvento").value("AGENDAMENTO_CRIADO"))
                .andExpect(jsonPath("$.data[0].sucesso").value(true));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve agendar via POST /api/marketing/schedule compativel com o frontend useMarketingStore")
    void deveAgendarViaEndpointMarketingSchedule() throws Exception {
        Map<String, Object> payload = Map.of(
                "masterShotUrl", "https://res.cloudinary.com/demo/image/upload/v1/sao_jeronimo.webp",
                "caption", "São Jerônimo #TattooFlow WhatsApp: https://wa.me/5511999999999",
                "scheduleDate", "24/MAI/2025",
                "scheduleTime", "20:45",
                "channels", Map.of("feed", true, "reels", false)
        );

        mockMvc.perform(post("/api/marketing/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocol").isNotEmpty())
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve retornar configuracoes de distribuicao via GET /api/marketing/distribuicao/settings")
    void deveRetornarConfiguracoesDistribuicao() throws Exception {
        mockMvc.perform(get("/api/marketing/distribuicao/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.activeAccountId").isNotEmpty())
                .andExpect(jsonPath("$.channels.feed").value(true));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve alternar conta do Instagram via POST /api/marketing/instagram/switch")
    void deveAlternarContaInstagram() throws Exception {
        mockMvc.perform(post("/api/marketing/instagram/switch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"acc-inst-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.activeAccountId").value("acc-inst-02"));
    }
}

