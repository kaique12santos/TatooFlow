package com.tattooflow.modules.cliente;

import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.midia.Midia;
import com.tattooflow.modules.midia.MidiaRepository;
import com.tattooflow.modules.termo.TermoAceite;
import com.tattooflow.modules.termo.TermoRepository;
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
        "spring.datasource.url=jdbc:h2:mem:cliente_busca_historico_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class ClienteBuscaEHistoricoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PreAgendamentoRepository preAgendamentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private TermoRepository termoRepository;

    @Autowired
    private MidiaRepository midiaRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    private String authorization;
    private Cliente clienteMarina;
    private Cliente clienteLucas;
    private Agendamento agendamentoMarina;
    private Midia midiaMarina;
    private TermoAceite termoMarina;

    @BeforeEach
    void prepararCenario() {
        usuarioService.salvar(Usuario.builder()
                .nome("Admin Cliente Test")
                .email("admin.cliente@tattooflow.com")
                .senha("123456")
                .aparelhoId("aparelho-cliente-01")
                .perfil("ADMIN")
                .ativo(true)
                .build());

        authorization = "Bearer " + jwtUtil.generateToken("admin.cliente@tattooflow.com");

        // 1. Cliente Marina
        clienteMarina = clienteRepository.save(Cliente.builder()
                .nome("Marina Silva")
                .telefone("11999990001")
                .email("marina.silva@email.com")
                .instagram("@marina_silva")
                .historicoSaude("Nenhuma alergia relatada")
                .build());

        // Sessão de agendamento de Marina com estilo Oriental
        agendamentoMarina = agendamentoRepository.save(Agendamento.builder()
                .clienteId(clienteMarina.getId())
                .tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 10, 14, 0))
                .duracaoMinutos(120)
                .descricaoSessao("Tatuagem Oriental Dragão")
                .valorEstimado(new BigDecimal("1200.00"))
                .status("CONFIRMADO")
                .build());

        // Mídia vinculada à sessão de Marina
        midiaMarina = midiaRepository.save(Midia.builder()
                .agendamentoId(agendamentoMarina.getId())
                .url("https://res.cloudinary.com/tattooflow/image/upload/v1/marina-dragao.jpg")
                .tipo("FOTO_DEPOIS")
                .dataUpload(LocalDateTime.of(2026, 11, 10, 16, 30))
                .build());

        // Termo de consentimento assinado por Marina
        termoMarina = termoRepository.save(TermoAceite.builder()
                .clienteId(clienteMarina.getId())
                .agendamentoId(agendamentoMarina.getId())
                .urlPdfAssinado("https://storage.tattooflow.com/termos/termo-marina.pdf")
                .dataAssinatura(LocalDateTime.of(2026, 11, 10, 13, 55))
                .ipAssinatura("192.168.1.50")
                .dataExpiracaoRetencao(LocalDateTime.of(2031, 11, 10, 13, 55))
                .build());

        // 2. Cliente Lucas
        clienteLucas = clienteRepository.save(Cliente.builder()
                .nome("Lucas Oliveira")
                .telefone("11988880002")
                .email("lucas.oliveira@email.com")
                .instagram("@lucas_tatoo")
                .historicoSaude("Diabetes controlada")
                .build());

        // Pré-agendamento (orçamento) de Lucas com estilo Realismo Preto e Cinza
        preAgendamentoRepository.save(PreAgendamento.builder()
                .cliente(clienteLucas)
                .ideiasDescricao("Realismo Preto e Cinza leão no antebraço")
                .referencia("https://pinterest.com/pin/exemplo-leao")
                .localCorpo("Antebraço esquerdo")
                .dataDesejada("2026-11-20")
                .statusTriagem("PENDENTE")
                .etapaBot("FINALIZADO")
                .dataCriacao(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build());
    }

    // =========================================================================
    // TESTES: F5-BACK-02 - Busca/filtro de clientes e orçamentos (UC04.1)
    // =========================================================================

    @Test
    @DisplayName("GET /api/clientes/busca?termo= deve encontrar cliente pelo nome")
    void deveBuscarClientePorNome() throws Exception {
        mockMvc.perform(get("/api/clientes/busca")
                        .param("termo", "Marina")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nome", is("Marina Silva")))
                .andExpect(jsonPath("$.data[0].email", is("marina.silva@email.com")))
                .andExpect(jsonPath("$.data[0].tipoTrabalho", is("Tatuagem Oriental Dragão")))
                .andExpect(jsonPath("$.data[0].sessoes", hasSize(1)))
                .andExpect(jsonPath("$.data[0].sessoes[0].descricaoSessao", is("Tatuagem Oriental Dragão")));
    }

    @Test
    @DisplayName("GET /clientes/busca (sem prefixo /api) deve funcionar com rota alias")
    void deveBuscarClientePelaRotaAlias() throws Exception {
        mockMvc.perform(get("/clientes/busca")
                        .param("termo", "Lucas")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nome", is("Lucas Oliveira")));
    }

    @Test
    @DisplayName("GET /api/clientes/busca?termo= deve encontrar cliente pelo tipo de trabalho na sessão (agendamento)")
    void deveBuscarClientePorTipoTrabalhoNaSessao() throws Exception {
        mockMvc.perform(get("/api/clientes/busca")
                        .param("termo", "Dragão")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nome", is("Marina Silva")));
    }

    @Test
    @DisplayName("GET /api/clientes/busca?termo= deve encontrar cliente pelo tipo de trabalho no orçamento (pré-agendamento)")
    void deveBuscarClientePorTipoTrabalhoNoPreAgendamento() throws Exception {
        mockMvc.perform(get("/api/clientes/busca")
                        .param("termo", "Realismo")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].nome", is("Lucas Oliveira")))
                .andExpect(jsonPath("$.data[0].orcamentos", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orcamentos[0].ideiasDescricao", containsString("Realismo")));
    }

    @Test
    @DisplayName("GET /api/clientes/busca sem termo deve listar todos os clientes ordenados")
    void deveBuscarTodosClientesQuandoTermoVazio() throws Exception {
        mockMvc.perform(get("/api/clientes/busca")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("GET /api/clientes/busca?termo=Inexistente deve retornar lista vazia")
    void deveRetornarVazioQuandoNaoEncontrarResultados() throws Exception {
        mockMvc.perform(get("/api/clientes/busca")
                        .param("termo", "TermoInexistenteXYZ")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // =========================================================================
    // TESTES: F5-BACK-03 - Histórico completo do cliente
    // =========================================================================

    @Test
    @DisplayName("GET /api/clientes/{id}/historico deve retornar dados do cliente, sessões, termos e mídias")
    void deveRetornarHistoricoCompletoDoCliente() throws Exception {
        mockMvc.perform(get("/api/clientes/" + clienteMarina.getId() + "/historico")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.cliente.id", is(clienteMarina.getId().intValue())))
                .andExpect(jsonPath("$.data.cliente.nome", is("Marina Silva")))
                .andExpect(jsonPath("$.data.totalSessoes", is(1)))
                .andExpect(jsonPath("$.data.totalTermos", is(1)))
                .andExpect(jsonPath("$.data.totalMidias", is(1)))
                .andExpect(jsonPath("$.data.sessoes", hasSize(1)))
                .andExpect(jsonPath("$.data.sessoes[0].descricaoSessao", is("Tatuagem Oriental Dragão")))
                .andExpect(jsonPath("$.data.sessoes[0].midias", hasSize(1)))
                .andExpect(jsonPath("$.data.sessoes[0].midias[0].url", containsString("marina-dragao.jpg")))
                .andExpect(jsonPath("$.data.termos", hasSize(1)))
                .andExpect(jsonPath("$.data.termos[0].urlPdfAssinado", containsString("termo-marina.pdf")))
                .andExpect(jsonPath("$.data.midias", hasSize(1)))
                .andExpect(jsonPath("$.data.midias[0].url", containsString("marina-dragao.jpg")));
    }

    @Test
    @DisplayName("GET /clientes/{id}/historico (sem prefixo /api) deve funcionar na rota alias")
    void deveRetornarHistoricoPelaRotaAlias() throws Exception {
        mockMvc.perform(get("/clientes/" + clienteMarina.getId() + "/historico")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.cliente.nome", is("Marina Silva")));
    }

    @Test
    @DisplayName("GET /api/clientes/{id}/historico para cliente inexistente deve retornar 404")
    void deveRetornar404ParaClienteInexistenteNoHistorico() throws Exception {
        mockMvc.perform(get("/api/clientes/99999/historico")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @DisplayName("GET /api/clientes/busca e /historico sem token devem retornar 403/401")
    void deveBloquearAcessoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/api/clientes/busca").param("termo", "Marina"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/clientes/" + clienteMarina.getId() + "/historico"))
                .andExpect(status().isForbidden());
    }
}

