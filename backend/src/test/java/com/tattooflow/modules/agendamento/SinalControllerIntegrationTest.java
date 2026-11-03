package com.tattooflow.modules.agendamento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sinal_route_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
@WithMockUser(roles = "TATUADOR")
class SinalControllerIntegrationTest {
    private static final byte[] PDF = "%PDF-1.4\ncomprovante".getBytes(StandardCharsets.UTF_8);
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AgendamentoRepository agendamentos;
    @Autowired
    private FinanceiroRepository pagamentos;
    @Autowired
    private ObjectMapper mapper;
    private Long id;

    @BeforeEach
    void preparar() {
        id = agendamentos.saveAndFlush(Agendamento.builder().clienteId(1L).tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 10, 14, 0)).duracaoMinutos(120)
                .status("AGENDADO").valorEstimado(new BigDecimal("350.00")).build()).getId();
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("comprovante", "sinal.pdf", "application/pdf", PDF);
    }

    @Test
    void deveRegistrarUploadConfirmarEPersistirComprovante() throws Exception {
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "175.00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.agendamentoId").value(id))
                .andExpect(jsonPath("$.data.valor").value(175.0))
                .andExpect(jsonPath("$.data.tipo").value("SINAL"))
                .andExpect(jsonPath("$.data.formaPagamento").value("PIX"))
                .andExpect(jsonPath("$.data.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.data.comprovanteNome").value("sinal.pdf"))
                .andExpect(jsonPath("$.data.comprovante").doesNotExist());
        assertEquals("CONFIRMADO", agendamentos.findById(id).orElseThrow().getStatus());
        assertArrayEquals(PDF, pagamentos.findByAgendamentoIdAndTipo(id, "SINAL").orElseThrow().getComprovante());

        mvc.perform(get("/api/agendamentos/{id}/sinal/comprovante", id))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment;")))
                .andExpect(content().bytes(PDF));
    }

    @Test
    void deveRecusarValorIncorretoSemConfirmarOuGravar() throws Exception {
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "100.00"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        assertEquals("AGENDADO", agendamentos.findById(id).orElseThrow().getStatus());
        assertFalse(pagamentos.existsByAgendamentoIdAndTipo(id, "SINAL"));
    }

    @Test
    void deveRecusarSinalDuplicado() throws Exception {
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "175.00"))
                .andExpect(status().isCreated());
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "175.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O sinal deste agendamento já foi registrado"));
    }

    @Test
    void deveExigirArquivoEValorValido() throws Exception {
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).param("valor", "175.00"))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404QuandoAgendamentoOuComprovanteNaoExiste() throws Exception {
        mvc.perform(multipart("/api/agendamentos/999999/sinal").file(pdf()).param("valor", "175.00"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/agendamentos/{id}/sinal/comprovante", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveExigirEnvioMultipart() throws Exception {
        mvc.perform(post("/api/agendamentos/{id}/sinal", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithAnonymousUser
    void deveExigirAutenticacaoParaUploadEDownload() throws Exception {
        mvc.perform(multipart("/api/agendamentos/{id}/sinal", id).file(pdf()).param("valor", "175.00"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/agendamentos/{id}/sinal/comprovante", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBloquearConfirmacaoDiretaPelaCriacaoOuEdicao() throws Exception {
        Agendamento novo = Agendamento.builder().clienteId(1L).tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 11, 14, 0)).duracaoMinutos(60)
                .valorEstimado(new BigDecimal("350.00")).status("CONFIRMADO").build();
        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(novo)))
                .andExpect(status().isBadRequest());
        novo.setId(id);
        mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(novo)))
                .andExpect(status().isBadRequest());
        assertEquals("AGENDADO", agendamentos.findById(id).orElseThrow().getStatus());
    }

    @Test
    void pagamentoComumNaoDeveConfirmarOuSubstituirSinal() throws Exception {
        Pagamento pagamento = Pagamento.builder().agendamentoId(id).valor(new BigDecimal("175.00")).build();
        mvc.perform(post("/api/financeiro/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(pagamento)))
                .andExpect(status().isOk());
        assertEquals("AGENDADO", agendamentos.findById(id).orElseThrow().getStatus());
        pagamento.setTipo("SINAL");
        mvc.perform(post("/api/financeiro/pagamentos").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(pagamento)))
                .andExpect(status().isBadRequest());
    }
}
