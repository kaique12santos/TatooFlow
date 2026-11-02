package com.tattooflow.modules.estoque;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/estoque", "/estoque"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Estoque", description = "CRUD de itens de estoque (F7-BACK-01) e alerta automático de estoque mínimo (F7-BACK-02, UC05.1)")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    @Operation(summary = "Listar itens de estoque",
            description = "Retorna todos os itens de estoque. Suporta filtros por termo, categoria e itens em nível crítico.")
    public ResponseEntity<ApiResponse<List<ItemEstoque>>> listarTodos(
            @Parameter(description = "Termo de busca por nome ou categoria")
            @RequestParam(required = false) String termo,
            @Parameter(description = "Categoria do insumo (AGULHAS, TINTAS, LUVAS, etc.)")
            @RequestParam(required = false) String categoria,
            @Parameter(description = "Se true, filtra apenas itens que atingiram ou estão abaixo da quantidade mínima")
            @RequestParam(required = false) Boolean apenasMinimo) {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.listar(termo, categoria, apenasMinimo)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item de estoque por ID")
    public ResponseEntity<ApiResponse<ItemEstoque>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo item de estoque [F7-BACK-01]",
            description = "Cadastra um insumo com validação de nome obrigatório e quantidade não-negativa.")
    public ResponseEntity<ApiResponse<ItemEstoque>> criar(@RequestBody ItemEstoque item) {
        ItemEstoque criado = estoqueService.criar(item);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item de estoque cadastrado com sucesso", criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar item de estoque [F7-BACK-01]",
            description = "Atualiza os dados de um insumo com validação de quantidades não-negativas.")
    public ResponseEntity<ApiResponse<ItemEstoque>> atualizar(
            @PathVariable Long id,
            @RequestBody ItemEstoque item) {
        ItemEstoque atualizado = estoqueService.atualizar(id, item);
        return ResponseEntity.ok(ApiResponse.success("Item de estoque atualizado com sucesso", atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover item de estoque [F7-BACK-01]",
            description = "Remove o insumo de estoque com base no ID informado.")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id) {
        estoqueService.deletar(id);
        return ResponseEntity.ok(ApiResponse.success("Item de estoque removido com sucesso", null));
    }

    @PostMapping("/{id}/baixa")
    @Operation(summary = "Dar baixa em item de estoque e verificar limite mínimo [F7-BACK-02, UC05.1]",
            description = "Realiza baixa no saldo de um insumo e dispara notificação automática ao administrador se atingir a quantidade mínima.")
    public ResponseEntity<ApiResponse<BaixaEstoqueResponse>> darBaixa(
            @PathVariable Long id,
            @RequestBody BaixaEstoqueRequest request) {
        Integer qtd = request != null ? request.getQuantidade() : null;
        String motivo = request != null ? request.getMotivo() : null;
        Long agendamentoId = request != null ? request.getAgendamentoId() : null;
        BaixaEstoqueResponse response = estoqueService.darBaixa(id, qtd, motivo, agendamentoId);
        return ResponseEntity.ok(ApiResponse.success(response.getMensagem(), response));
    }

    @PostMapping("/alocar")
    @Operation(summary = "Alocação de múltiplos insumos para sessão [F7-BACK-02, UC05.1]",
            description = "Permite baixar múltiplos insumos simultaneamente disparando alertas para os itens que atingirem o limite mínimo.")
    public ResponseEntity<ApiResponse<List<BaixaEstoqueResponse>>> alocarMateriais(@RequestBody AlocacaoEstoqueRequest request) {
        List<BaixaEstoqueResponse> response = estoqueService.alocarMateriais(request);
        return ResponseEntity.ok(ApiResponse.success("Alocação de materiais concluída", response));
    }

    @GetMapping("/alertas/minimo")
    @Operation(summary = "Listar insumos críticos em estoque mínimo [F7-BACK-02]",
            description = "Retorna todos os itens que atingiram ou estão abaixo da quantidade mínima estipulada.")
    public ResponseEntity<ApiResponse<List<ItemEstoque>>> listarItensCriticos() {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.listarItensCriticos()));
    }

    @GetMapping("/alertas/historico")
    @Operation(summary = "Consultar histórico de alertas disparados ao administrador [F7-BACK-02]")
    public ResponseEntity<ApiResponse<List<AlertaEstoqueDTO>>> listarHistoricoAlertas() {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.listarHistoricoAlertas()));
    }
}
