package com.tattooflow.modules.estoque;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:estoque_integration_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class EstoqueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        usuarioService.salvar(Usuario.builder()
                .nome("Administrador Estoque")
                .email("admin_estoque_test@tattooflow.com")
                .senha("123456")
                .aparelhoId("dev-device-estoque-01")
                .perfil("ADMIN")
                .ativo(true)
                .build());

        adminToken = "Bearer " + jwtUtil.generateToken("admin_estoque_test@tattooflow.com");
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve cadastrar item de estoque com sucesso via POST /api/estoque")
    void deveCadastrarItemEstoqueComSucesso() throws Exception {
        ItemEstoque novo = ItemEstoque.builder()
                .nome("Cartucho 05RL Cheyenne")
                .categoria("AGULHAS")
                .quantidade(25)
                .quantidadeMinima(5)
                .unidadeMedida("unidades")
                .build();

        mockMvc.perform(post("/api/estoque")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.nome").value("Cartucho 05RL Cheyenne"))
                .andExpect(jsonPath("$.data.quantidade").value(25))
                .andExpect(jsonPath("$.data.quantidadeMinima").value(5));
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve rejeitar cadastro com quantidade negativa")
    void deveRejeitarCadastroComQuantidadeNegativa() throws Exception {
        ItemEstoque invalido = ItemEstoque.builder()
                .nome("Tinta Vermelha")
                .quantidade(-10)
                .quantidadeMinima(2)
                .build();

        mockMvc.perform(post("/api/estoque")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("A quantidade em estoque não pode ser negativa")));
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve atualizar item de estoque via PUT /api/estoque/{id}")
    void deveAtualizarItemEstoqueComSucesso() throws Exception {
        ItemEstoque salvo = estoqueRepository.save(ItemEstoque.builder()
                .nome("Luva Nitrílica P")
                .categoria("LUVAS")
                .quantidade(50)
                .quantidadeMinima(10)
                .unidadeMedida("pares")
                .build());

        ItemEstoque dadosAtualizados = ItemEstoque.builder()
                .nome("Luva Nitrílica M (Atualizada)")
                .categoria("LUVAS")
                .quantidade(60)
                .quantidadeMinima(15)
                .unidadeMedida("pares")
                .build();

        mockMvc.perform(put("/api/estoque/" + salvo.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dadosAtualizados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Luva Nitrílica M (Atualizada)"))
                .andExpect(jsonPath("$.data.quantidade").value(60))
                .andExpect(jsonPath("$.data.quantidadeMinima").value(15));
    }

    @Test
    @DisplayName("[F7-BACK-01] Deve remover item de estoque via DELETE /api/estoque/{id}")
    void deveRemoverItemEstoque() throws Exception {
        ItemEstoque salvo = estoqueRepository.save(ItemEstoque.builder()
                .nome("Item Temporário")
                .quantidade(10)
                .quantidadeMinima(2)
                .build());

        mockMvc.perform(delete("/api/estoque/" + salvo.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/estoque/" + salvo.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve realizar baixa no estoque e acionar alerta quando atingir quantidade mínima")
    void deveDarBaixaEDispararAlertaEstoqueMinimo() throws Exception {
        ItemEstoque item = estoqueRepository.save(ItemEstoque.builder()
                .nome("Tinta Dynamic Black 120ml")
                .categoria("TINTAS")
                .quantidade(10)
                .quantidadeMinima(5)
                .unidadeMedida("frascos")
                .build());

        // Baixa de 5: saldo cai de 10 para 5 (atinge o mínimo de 5 -> alerta automático)
        BaixaEstoqueRequest baixaReq = BaixaEstoqueRequest.builder()
                .quantidade(5)
                .motivo("Uso na sessão")
                .build();

        mockMvc.perform(post("/api/estoque/" + item.getId() + "/baixa")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baixaReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.saldoAnterior").value(10))
                .andExpect(jsonPath("$.data.saldoAtual").value(5))
                .andExpect(jsonPath("$.data.atingiuEstoqueMinimo").value(true))
                .andExpect(jsonPath("$.data.alertaDisparado").value(true))
                .andExpect(jsonPath("$.data.alerta.quantidadeAtual").value(5))
                .andExpect(jsonPath("$.data.alerta.quantidadeMinima").value(5));

        // Consulta de itens críticos
        mockMvc.perform(get("/api/estoque/alertas/minimo")
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[?(@.id == " + item.getId() + ")]").exists());
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve rejeitar baixa de estoque quando saldo for insuficiente")
    void deveRejeitarBaixaComSaldoInsuficiente() throws Exception {
        ItemEstoque item = estoqueRepository.save(ItemEstoque.builder()
                .nome("Agulha 03RL")
                .quantidade(4)
                .quantidadeMinima(2)
                .build());

        BaixaEstoqueRequest baixaReq = BaixaEstoqueRequest.builder()
                .quantidade(10)
                .motivo("Tentativa excessiva")
                .build();

        mockMvc.perform(post("/api/estoque/" + item.getId() + "/baixa")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baixaReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Saldo insuficiente em estoque")));
    }

    @Test
    @DisplayName("[F7-BACK-02] Deve alocar materiais de sessão em lote via POST /api/estoque/alocar")
    void deveAlocarMateriaisEmLote() throws Exception {
        ItemEstoque item1 = estoqueRepository.save(ItemEstoque.builder().nome("Item Lote 1").quantidade(20).quantidadeMinima(5).build());
        ItemEstoque item2 = estoqueRepository.save(ItemEstoque.builder().nome("Item Lote 2").quantidade(6).quantidadeMinima(5).build());

        AlocacaoEstoqueRequest request = AlocacaoEstoqueRequest.builder()
                .sessionId("sessao-101")
                .items(List.of(
                        new AlocacaoItemPayload(item1.getId(), "Item Lote 1", 3), // saldo: 17
                        new AlocacaoItemPayload(item2.getId(), "Item Lote 2", 2)  // saldo: 4 (atinge < 5 -> alerta)
                ))
                .build();

        mockMvc.perform(post("/api/estoque/alocar")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].saldoAtual").value(17))
                .andExpect(jsonPath("$.data[0].atingiuEstoqueMinimo").value(false))
                .andExpect(jsonPath("$.data[1].saldoAtual").value(4))
                .andExpect(jsonPath("$.data[1].atingiuEstoqueMinimo").value(true));
    }
}

