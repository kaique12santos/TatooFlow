package com.tattooflow.modules.financeiro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.security.JwtUtil;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:financeiro_integration_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class FinanceiroAgendamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private RepasseRepository repasseRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    private String adminToken;
    private Cliente clienteTeste;
    private Usuario tatuadorTeste;

    @BeforeEach
    void setUp() {
        tatuadorTeste = usuarioService.salvar(Usuario.builder()
                .nome("Tatuador Master Financeiro")
                .email("admin_fin_test@tattooflow.com")
                .senha("123456")
                .aparelhoId("dev-device-fin-01")
                .perfil("ADMIN")
                .ativo(true)
                .build());

        adminToken = "Bearer " + jwtUtil.generateToken("admin_fin_test@tattooflow.com");

        clienteTeste = clienteRepository.save(Cliente.builder()
                .nome("Cliente Teste Financeiro")
                .email("cliente_fin_test@gmail.com")
                .telefone("11988887777")
                .instagram("@cliente_fin")
                .historicoSaude("Nenhum histórico alérgico")
                .build());
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve calcular divisão 30/70 deduzindo insumos via POST /api/financeiro/divisao/calcular")
    void deveCalcularDivisaoViaEndpoint() throws Exception {
        CalculoDivisaoRequest request = CalculoDivisaoRequest.builder()
                .valorTotal(new BigDecimal("1000.00"))
                .custoInsumos(new BigDecimal("200.00"))
                .build();

        mockMvc.perform(post("/api/financeiro/divisao/calcular")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.valorTotal").value(1000.00))
                .andExpect(jsonPath("$.data.custoInsumos").value(200.00))
                .andExpect(jsonPath("$.data.baseCalculo").value(800.00))
                .andExpect(jsonPath("$.data.porcentagemEstudio").value(30.00))
                .andExpect(jsonPath("$.data.valorEstudio").value(240.00))
                .andExpect(jsonPath("$.data.porcentagemArtista").value(70.00))
                .andExpect(jsonPath("$.data.valorArtista").value(560.00));
    }

    @Test
    @DisplayName("[F6-BACK-01] Deve finalizar sessão de agendamento e gerar repasse persistido no banco")
    void deveFinalizarSessaoEGravarRepasse() throws Exception {
        Agendamento agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(clienteTeste.getId())
                .tatuadorId(tatuadorTeste.getId())
                .dataHora(LocalDateTime.now().plusDays(2))
                .duracaoMinutos(120)
                .descricaoSessao("Tatuagem Dragão Oriental")
                .valorEstimado(new BigDecimal("800.00"))
                .status("CONFIRMADO")
                .build());

        // Registra o sinal
        financeiroRepository.save(Pagamento.builder()
                .agendamentoId(agendamento.getId())
                .valor(new BigDecimal("400.00"))
                .tipo("SINAL")
                .formaPagamento("PIX")
                .status("CONCLUIDO")
                .dataPagamento(LocalDateTime.now())
                .build());

        FinalizarSessaoRequest request = FinalizarSessaoRequest.builder()
                .custoInsumos(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/agendamentos/" + agendamento.getId() + "/finalizar")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.agendamento.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.data.divisao.baseCalculo").value(700.00))
                .andExpect(jsonPath("$.data.divisao.valorEstudio").value(210.00))
                .andExpect(jsonPath("$.data.divisao.valorArtista").value(490.00))
                .andExpect(jsonPath("$.data.repasse.valorRepasse").value(490.00))
                .andExpect(jsonPath("$.data.repasse.status").value("PENDENTE"));

        // Validar consulta do repasse na rota GET
        mockMvc.perform(get("/api/financeiro/repasses/agendamento/" + agendamento.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].valorRepasse").value(490.00));
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve consultar reembolso com antecedência >= 48h com reembolso integral permitido (RN05)")
    void deveConsultarReembolsoComMaisDe48h() throws Exception {
        LocalDateTime dataSessao = LocalDateTime.now().plusDays(5); // 120 horas
        Agendamento agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(clienteTeste.getId())
                .tatuadorId(tatuadorTeste.getId())
                .dataHora(dataSessao)
                .duracaoMinutos(60)
                .valorEstimado(new BigDecimal("500.00"))
                .status("CONFIRMADO")
                .build());

        financeiroRepository.save(Pagamento.builder()
                .agendamentoId(agendamento.getId())
                .valor(new BigDecimal("250.00"))
                .tipo("SINAL")
                .status("CONCLUIDO")
                .dataPagamento(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/agendamentos/" + agendamento.getId() + "/reembolso")
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menosDe48Horas").value(false))
                .andExpect(jsonPath("$.data.reembolsoIntegralPermitido").value(true))
                .andExpect(jsonPath("$.data.valorPago").value(250.00))
                .andExpect(jsonPath("$.data.valorDesconto").value(0.00))
                .andExpect(jsonPath("$.data.valorReembolsavel").value(250.00));
    }

    @Test
    @DisplayName("[F6-BACK-02] Deve aplicar desconto e bloquear reembolso integral quando cancelado com menos de 48h (RN05)")
    void deveCancelarComMenosDe48hEAplicarDesconto() throws Exception {
        // Agendamento daqui a 24 horas (< 48h)
        LocalDateTime dataSessao = LocalDateTime.now().plusHours(24);
        Agendamento agendamento = agendamentoRepository.save(Agendamento.builder()
                .clienteId(clienteTeste.getId())
                .tatuadorId(tatuadorTeste.getId())
                .dataHora(dataSessao)
                .duracaoMinutos(60)
                .valorEstimado(new BigDecimal("600.00"))
                .status("CONFIRMADO")
                .build());

        financeiroRepository.save(Pagamento.builder()
                .agendamentoId(agendamento.getId())
                .valor(new BigDecimal("300.00"))
                .tipo("SINAL")
                .status("CONCLUIDO")
                .dataPagamento(LocalDateTime.now())
                .build());

        // 1. Simulação: deve acusar menosDe48Horas = true e taxa de 50%
        mockMvc.perform(get("/api/agendamentos/" + agendamento.getId() + "/reembolso")
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menosDe48Horas").value(true))
                .andExpect(jsonPath("$.data.reembolsoIntegralPermitido").value(false))
                .andExpect(jsonPath("$.data.valorDesconto").value(150.00))
                .andExpect(jsonPath("$.data.valorReembolsavel").value(150.00));

        // 2. Tentativa de forçar reembolso integral deve ser bloqueada (RN05)
        CancelarAgendamentoRequest reqForcado = CancelarAgendamentoRequest.builder()
                .forcarReembolsoIntegral(true)
                .build();

        mockMvc.perform(post("/api/agendamentos/" + agendamento.getId() + "/cancelar")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqForcado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("RN05")));

        // 3. Cancelamento normal com menos de 48h aplicando desconto automático
        CancelarAgendamentoRequest reqValido = CancelarAgendamentoRequest.builder()
                .motivo("Imprevisto")
                .build();

        mockMvc.perform(post("/api/agendamentos/" + agendamento.getId() + "/cancelar")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agendamento.status").value("CANCELADO"))
                .andExpect(jsonPath("$.data.reembolso.valorReembolsavel").value(150.00))
                .andExpect(jsonPath("$.data.pagamentoReembolso.tipo").value("REEMBOLSO"))
                .andExpect(jsonPath("$.data.pagamentoReembolso.valor").value(150.00));
    }
}
