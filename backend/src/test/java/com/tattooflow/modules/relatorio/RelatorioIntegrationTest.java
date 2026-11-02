package com.tattooflow.modules.relatorio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import com.tattooflow.modules.estoque.EstoqueRepository;
import com.tattooflow.modules.estoque.ItemEstoque;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import com.tattooflow.modules.financeiro.Repasse;
import com.tattooflow.modules.financeiro.RepasseRepository;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
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
        "spring.datasource.url=jdbc:h2:mem:relatorio_integration_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class RelatorioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private RepasseRepository repasseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private Usuario tatuador;

    @BeforeEach
    void setUp() {
        Usuario admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin Relatórios")
                .email("admin_relatorios@tattooflow.com")
                .senha("hash-senha-admin")
                .aparelhoId("device-relatorios-admin")
                .perfil("ADMIN")
                .ativo(true)
                .build());

        tatuador = usuarioRepository.save(Usuario.builder()
                .nome("Artista Lucas")
                .email("lucas_tatuador@tattooflow.com")
                .senha("hash-senha-tatuador")
                .aparelhoId("device-relatorios-tatuador")
                .perfil("TATUADOR")
                .ativo(true)
                .build());

        adminToken = "Bearer " + jwtUtil.generateToken("admin_relatorios@tattooflow.com");
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve retornar 401 ou 403 ao tentar acessar relatório sem autenticação")
    void deveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal")
                        .param("mes", "11")
                        .param("ano", "2026"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve rejeitar mês inválido com HTTP 400")
    void deveRejeitarMesInvalido() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("mes", "13")
                        .param("ano", "2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Mês inválido")));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve obter relatório mensal vazio com sucesso via GET /api/relatorios/mensal")
    void deveObterRelatorioMensalVazioComSucesso() throws Exception {
        mockMvc.perform(get("/api/relatorios/mensal")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("mes", "11")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mes").value(11))
                .andExpect(jsonPath("$.data.ano").value(2026))
                .andExpect(jsonPath("$.data.periodo").value("11/2026"))
                .andExpect(jsonPath("$.data.totalFaturamento").value(0.0))
                .andExpect(jsonPath("$.data.totalComissoes").value(0.0))
                .andExpect(jsonPath("$.data.totalRepasses").value(0.0))
                .andExpect(jsonPath("$.data.totalGastos").value(0.0))
                .andExpect(jsonPath("$.data.totalSessoes").value(0));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve suportar rota direta /relatorios/mensal sem prefixo /api")
    void deveSuportarRotaSemPrefixoApi() throws Exception {
        mockMvc.perform(get("/relatorios/mensal")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("mes", "11")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mes").value(11));
    }

    @Test
    @DisplayName("[F8-BACK-01] Deve consolidar faturamento, comissões, repasses e insumos no banco de dados")
    void deveConsolidarDadosReaisNoBanco() throws Exception {
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nome("Cliente Relatório")
                .telefone("11999990000")
                .build());

        LocalDateTime dataNovembro = LocalDateTime.of(2026, 11, 10, 15, 0);

        Agendamento agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(cliente.getId())
                .tatuadorId(tatuador.getId())
                .dataHora(dataNovembro)
                .duracaoMinutos(180)
                .descricaoSessao("Tatuagem Dragão Oriental")
                .valorEstimado(new BigDecimal("1200.00"))
                .status("CONCLUIDO")
                .build());

        // Insumos = 200 -> Base = 1000 -> Artista 700 (70%) / Estúdio 300 (30%)
        repasseRepository.save(Repasse.builder()
                .agendamentoId(agendamento.getId())
                .tatuadorId(tatuador.getId())
                .custoInsumos(new BigDecimal("200.00"))
                .valorRepasse(new BigDecimal("700.00"))
                .valorLiquido(new BigDecimal("700.00"))
                .porcentagemEstudio(new BigDecimal("30.00"))
                .dataRepasse(dataNovembro)
                .status("CONCLUIDO")
                .build());

        financeiroRepository.save(Pagamento.builder()
                .agendamentoId(agendamento.getId())
                .valor(new BigDecimal("1200.00"))
                .status("CONCLUIDO")
                .tipo("PAGAMENTO")
                .dataPagamento(dataNovembro)
                .build());

        estoqueRepository.save(ItemEstoque.builder()
                .nome("Agulha 03RL")
                .categoria("AGULHAS")
                .quantidade(2)
                .quantidadeMinima(5)
                .unidadeMedida("unidades")
                .build());

        mockMvc.perform(get("/api/relatorios/mensal")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .param("mes", "11")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mes").value(11))
                .andExpect(jsonPath("$.data.ano").value(2026))
                .andExpect(jsonPath("$.data.totalFaturamento").value(1200.00))
                .andExpect(jsonPath("$.data.totalComissoes").value(300.00))
                .andExpect(jsonPath("$.data.totalRepasses").value(700.00))
                .andExpect(jsonPath("$.data.totalGastos").value(200.00))
                .andExpect(jsonPath("$.data.totalGastosInsumos").value(200.00))
                .andExpect(jsonPath("$.data.lucroLiquido").value(300.00))
                .andExpect(jsonPath("$.data.totalSessoes").value(1))
                .andExpect(jsonPath("$.data.detalhamentoArtistas", hasSize(1)))
                .andExpect(jsonPath("$.data.detalhamentoArtistas[0].nomeTatuador").value("Artista Lucas"))
                .andExpect(jsonPath("$.data.detalhamentoArtistas[0].totalFaturado").value(1200.00))
                .andExpect(jsonPath("$.data.detalhamentoArtistas[0].comissaoEstudio").value(300.00))
                .andExpect(jsonPath("$.data.detalhamentoArtistas[0].repasseArtista").value(700.00))
                .andExpect(jsonPath("$.data.detalhamentoArtistas[0].custoInsumos").value(200.00))
                .andExpect(jsonPath("$.data.resumoEstoque.itensEmEstoqueCritico").value(1))
                .andExpect(jsonPath("$.data.resumoEstoque.alertaEstoqueAtivo").value(true));
    }
}
