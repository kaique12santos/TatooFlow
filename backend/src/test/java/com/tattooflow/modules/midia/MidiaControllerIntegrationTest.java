package com.tattooflow.modules.midia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import com.tattooflow.modules.termo.TermoAceite;
import com.tattooflow.modules.termo.TermoRepository;
import com.tattooflow.service.GeminiService;
import com.tattooflow.service.ImagemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:midia_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
@WithMockUser(roles = "TATUADOR")
class MidiaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MidiaRepository midiaRepository;

    @Autowired
    private TermoRepository termoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @MockBean
    private ImagemService imagemService;

    @MockBean
    private GeminiService geminiService;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente cliente;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(Cliente.builder()
                .nome("Carlos Silva")
                .telefone("11977778888")
                .email("carlos@exemplo.com")
                .build());

        agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(cliente.getId())
                .tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 25, 14, 0))
                .duracaoMinutos(180)
                .status("CONFIRMADO")
                .valorEstimado(new BigDecimal("500.00"))
                .build());

        when(imagemService.uploadImagem(any())).thenReturn("https://res.cloudinary.com/demo/image/upload/v1/tatoo_final.webp");

        LegendaResponse mockLegenda = LegendaResponse.builder()
                .title("Estudo de São Jerônimo · Chiaroscuro")
                .body("A profundidade do claro-escuro na derme. Estilo: FineLine. Localização: São Paulo - SP. WhatsApp: https://wa.me/5511999999999")
                .hashtags(List.of("#TattooFlow", "#FineLine", "#Chiaroscuro"))
                .legendaCompleta("A profundidade do claro-escuro na derme. Estilo: FineLine. Localização: São Paulo - SP. WhatsApp: https://wa.me/5511999999999\n\n#TattooFlow #FineLine #Chiaroscuro")
                .estilo("FineLine")
                .localizacao("São Paulo - SP")
                .whatsappLink("https://wa.me/5511999999999")
                .geradoPorIa(true)
                .fallback(false)
                .build();
        when(geminiService.gerarLegenda(any())).thenReturn(mockLegenda);
    }

    @Test
    @DisplayName("Deve realizar upload com sucesso via POST /midias/upload quando termo estiver assinado")
    void deveRealizarUploadComSucessoViaPostMidiasUpload() throws Exception {
        // Registra termo aceito
        termoRepository.save(TermoAceite.builder()
                .agendamentoId(agendamento.getId())
                .clienteId(cliente.getId())
                .urlPdfAssinado("https://storage/termo_assinado.pdf")
                .dataAssinatura(LocalDateTime.now())
                .ipAssinatura("127.0.0.1")
                .build());

        MockMultipartFile file = new MockMultipartFile(
                "file", "tatuagem.jpg", "image/jpeg", "conteudo-imagem".getBytes()
        );

        mockMvc.perform(multipart("/midias/upload")
                        .file(file)
                        .param("agendamentoId", agendamento.getId().toString())
                        .param("tipo", "FOTO_DEPOIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/demo/image/upload/v1/tatoo_final.webp"))
                .andExpect(jsonPath("$.data.tipo").value("FOTO_DEPOIS"))
                .andExpect(jsonPath("$.data.agendamentoId").value(agendamento.getId()));

        verify(imagemService, times(1)).uploadImagem(any());
        assertThat(midiaRepository.findByAgendamentoId(agendamento.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("Deve realizar upload com sucesso via POST /api/midias/upload com param 'arquivo'")
    void deveRealizarUploadComSucessoViaPostApiMidiasUpload() throws Exception {
        // Registra termo aceito
        termoRepository.save(TermoAceite.builder()
                .agendamentoId(agendamento.getId())
                .clienteId(cliente.getId())
                .urlPdfAssinado("https://storage/termo_assinado.pdf")
                .dataAssinatura(LocalDateTime.now())
                .ipAssinatura("127.0.0.1")
                .build());

        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "tatuagem.png", "image/png", "conteudo-imagem-png".getBytes()
        );

        mockMvc.perform(multipart("/api/midias/upload")
                        .file(file)
                        .param("agendamentoId", agendamento.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value("https://res.cloudinary.com/demo/image/upload/v1/tatoo_final.webp"))
                .andExpect(jsonPath("$.data.tipo").value("FOTO_DEPOIS"));

        verify(imagemService, times(1)).uploadImagem(any());
    }

    @Test
    @DisplayName("Deve bloquear upload quando cliente ainda não aceitou o termo digitalmente (Fase 4)")
    void deveBloquearUploadQuandoTermoNaoFoiAceito() throws Exception {
        // NÃO salva termo assinado

        MockMultipartFile file = new MockMultipartFile(
                "file", "tatuagem.jpg", "image/jpeg", "conteudo-imagem".getBytes()
        );

        mockMvc.perform(multipart("/midias/upload")
                        .file(file)
                        .param("agendamentoId", agendamento.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Upload de mídia bloqueado: o cliente deve aceitar digitalmente o termo de consentimento antes de enviar fotos (Fase 4)."));

        verify(imagemService, never()).uploadImagem(any());
        assertThat(midiaRepository.findByAgendamentoId(agendamento.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deve validar limite de 10MB rejeitando arquivos maiores")
    void deveRejeitarArquivoMaiorQue10MB() throws Exception {
        byte[] bytesGrandes = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile fileGrande = new MockMultipartFile(
                "file", "grande.jpg", "image/jpeg", bytesGrandes
        );

        mockMvc.perform(multipart("/midias/upload")
                        .file(fileGrande)
                        .param("agendamentoId", agendamento.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo excede o tamanho máximo permitido de 10MB."));

        verify(imagemService, never()).uploadImagem(any());
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve gerar legenda via POST /midias/legenda com sucesso e conformidade RN03")
    void deveGerarLegendaViaPostMidiasLegendaComSucesso() throws Exception {
        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .agendamentoId(agendamento.getId())
                .estilo("FineLine")
                .localizacao("São Paulo - SP")
                .whatsappLink("https://wa.me/5511999999999")
                .tom("chiaroscuro")
                .build();

        mockMvc.perform(post("/midias/legenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Estudo de São Jerônimo · Chiaroscuro"))
                .andExpect(jsonPath("$.data.estilo").value("FineLine"))
                .andExpect(jsonPath("$.data.whatsappLink").value("https://wa.me/5511999999999"))
                .andExpect(jsonPath("$.data.localizacao").value("São Paulo - SP"))
                .andExpect(jsonPath("$.data.geradoPorIa").value(true));

        verify(geminiService, times(1)).gerarLegenda(any());
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve gerar legenda para mídia existente via POST /api/midias/{id}/legenda")
    void deveGerarLegendaPorMidiaIdComSucesso() throws Exception {
        Midia midiaSalva = midiaRepository.save(Midia.builder()
                .agendamentoId(agendamento.getId())
                .url("https://res.cloudinary.com/demo/image/upload/v1/obra_salva.webp")
                .tipo("FOTO_DEPOIS")
                .build());

        mockMvc.perform(post("/api/midias/" + midiaSalva.getId() + "/legenda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.body").isNotEmpty());

        verify(geminiService, times(1)).gerarLegenda(any());
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve responder endpoint de curadoria do frontend via POST /api/marketing/curadoria-ia")
    void deveResponderEndpointCuradoriaIaDoFrontend() throws Exception {
        GerarLegendaRequest request = GerarLegendaRequest.builder()
                .tom("chiaroscuro")
                .imageUrl("/assets/sao_jeronimo_tattoo.jpg")
                .build();

        mockMvc.perform(post("/api/marketing/curadoria-ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Estudo de São Jerônimo · Chiaroscuro"))
                .andExpect(jsonPath("$.body").isNotEmpty())
                .andExpect(jsonPath("$.hashtags").isArray());

        verify(geminiService, times(1)).gerarLegenda(any());
    }

    @Test
    @DisplayName("[F4-BACK-02] Deve sincronizar rascunho de curadoria via GET /api/marketing/curadoria")
    void deveSincronizarRascunhoCuradoria() throws Exception {
        mockMvc.perform(get("/api/marketing/curadoria")
                        .param("sessionId", agendamento.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.body").isNotEmpty());

        verify(geminiService, times(1)).gerarLegenda(any());
    }
}
