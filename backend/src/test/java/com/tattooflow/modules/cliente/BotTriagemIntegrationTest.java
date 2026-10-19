package com.tattooflow.modules.cliente;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bot_triagem_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "integrations.whatsapp.enabled=false",
        "integrations.whatsapp.mock.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class BotTriagemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PreAgendamentoRepository preAgendamentoRepository;

    @Autowired
    private WhatsAppMessageSender messageSender;

    @BeforeEach
    void setUp() {
        messageSender.limparHistorico();
    }

    @Test
    void deveExecutarFluxoCompletoDeTriagemPeloEndpointSimular() throws Exception {
        String telefone = "5511999995555";
        String nome = "Roberto Alves";

        // Mensagem 1: Ideia da arte
        mockMvc.perform(post("/api/triagem/bot/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "telefone", telefone,
                                "nome", nome,
                                "mensagem", "Quero uma tattoo de lobo tribal"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.etapaAtual").value("AGUARDANDO_LOCAL"));

        // Mensagem 2: Resposta fora do padrão para o local (deve ser rejeitada)
        mockMvc.perform(post("/api/triagem/bot/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "telefone", telefone,
                                "nome", nome,
                                "mensagem", "12"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.etapaAtual").value("AGUARDANDO_LOCAL"))
                .andExpect(jsonPath("$.data.textoEnviado").value(org.hamcrest.Matchers.containsString("região do corpo válida")));

        // Mensagem 3: Local correto
        mockMvc.perform(post("/api/triagem/bot/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "telefone", telefone,
                                "nome", nome,
                                "mensagem", "Panturrilha esquerda"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.etapaAtual").value("AGUARDANDO_DATA"));

        // Mensagem 4: Data desejada (Finaliza)
        mockMvc.perform(post("/api/triagem/bot/simular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "telefone", telefone,
                                "nome", nome,
                                "mensagem", "20 de Novembro à tarde"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.etapaAtual").value("FINALIZADO"))
                .andExpect(jsonPath("$.data.textoEnviado").value(org.hamcrest.Matchers.containsString("concluído com sucesso")));

        // Valida persistência no banco
        Cliente cliente = clienteRepository.findByTelefone(telefone).orElseThrow();
        assertThat(cliente.getNome()).isEqualTo(nome);

        PreAgendamento pre = preAgendamentoRepository.findFirstByClienteOrderByDataCriacaoDesc(cliente).orElseThrow();
        assertThat(pre.getIdeiasDescricao()).isEqualTo("Quero uma tattoo de lobo tribal");
        assertThat(pre.getLocalCorpo()).isEqualTo("Panturrilha esquerda");
        assertThat(pre.getDataDesejada()).isEqualTo("20 de Novembro à tarde");
        assertThat(pre.getStatusTriagem()).isEqualTo("PENDENTE");
        assertThat(pre.getEtapaBot()).isEqualTo("FINALIZADO");
    }

    @Test
    void deveProcessarMensagensVindasDoMockWhatsAppViaEvento() throws Exception {
        String telefone = "5511999996666";

        // Simula mensagem via POST /api/whatsapp/mock/messages (F2-INFRA-02)
        mockMvc.perform(post("/api/whatsapp/mock/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "telefone", telefone,
                                "texto", "Quero uma fênix realista no peito"
                        ))))
                .andExpect(status().isOk());

        // Confirma que o cliente e o pre-agendamento foram criados via evento do WhatsApp
        Cliente cliente = clienteRepository.findByTelefone(telefone).orElseThrow();
        PreAgendamento pre = preAgendamentoRepository.findFirstByClienteOrderByDataCriacaoDesc(cliente).orElseThrow();

        assertThat(pre.getIdeiasDescricao()).isEqualTo("Quero uma fênix realista no peito");
        assertThat(pre.getEtapaBot()).isEqualTo("AGUARDANDO_LOCAL");

        // Confirma que a mensagem de resposta do bot foi enviada
        assertThat(messageSender.getHistoricoEnvios())
                .anyMatch(m -> m.telefone().equals(telefone) && m.texto().contains("local do corpo"));
    }
}
