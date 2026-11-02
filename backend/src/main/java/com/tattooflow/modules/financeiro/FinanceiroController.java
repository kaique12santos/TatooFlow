package com.tattooflow.modules.financeiro;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/financeiro", "/financeiro"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Financeiro", description = "Gestão financeira, pagamentos, repasses 30/70 (RN02/RN07) e política de cancelamento/reembolso (RN05)")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/pagamentos")
    @Operation(summary = "Listar pagamentos registrados")
    public ResponseEntity<ApiResponse<List<Pagamento>>> listarPagamentos() {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.listarPagamentos()));
    }

    @PostMapping("/pagamentos")
    @Operation(summary = "Registrar pagamento avulso")
    public ResponseEntity<ApiResponse<Pagamento>> registrarPagamento(@RequestBody Pagamento pagamento) {
        return ResponseEntity.ok(ApiResponse.success("Pagamento registrado", financeiroService.registrarPagamento(pagamento)));
    }

    @PostMapping("/divisao/calcular")
    @Operation(summary = "Calcular divisão 30% estúdio / 70% artista com dedução prévia de insumos [F6-BACK-01, RN02, RN07]",
            description = "Calcula a divisão automática entre estúdio e artista deduzindo previamente o custo de insumos (RN07).")
    public ResponseEntity<ApiResponse<DivisaoSessaoDTO>> calcularDivisao(@RequestBody CalculoDivisaoRequest request) {
        DivisaoSessaoDTO resultado;
        if (request.getAgendamentoId() != null) {
            resultado = financeiroService.simularDivisaoAgendamento(request.getAgendamentoId(), request.getCustoInsumos(), request.getValorTotal());
        } else {
            resultado = financeiroService.calcularDivisao(request.getValorTotal(), request.getCustoInsumos());
        }
        return ResponseEntity.ok(ApiResponse.success("Cálculo de divisão apurado com sucesso", resultado));
    }

    @PostMapping("/repasses/finalizar-sessao/{agendamentoId}")
    @Operation(summary = "Finalizar sessão e registrar repasse com divisão 30/70 e insumos [F6-BACK-01, RN02, RN07]",
            description = "Conclui a sessão do agendamento, apura a base líquida descontando insumos e gera o repasse de 70% ao tatuador.")
    public ResponseEntity<ApiResponse<FinalizarSessaoResponse>> finalizarSessao(
            @PathVariable Long agendamentoId,
            @RequestBody(required = false) FinalizarSessaoRequest request) {
        BigDecimal insumos = request != null ? request.getCustoInsumos() : null;
        BigDecimal valorTotal = request != null ? request.getValorTotal() : null;
        FinalizarSessaoResponse response = financeiroService.finalizarSessao(agendamentoId, insumos, valorTotal);
        return ResponseEntity.ok(ApiResponse.success("Sessão finalizada e repasse registrado com sucesso", response));
    }

    @GetMapping("/repasses")
    @Operation(summary = "Listar todos os repasses de tatuadores")
    public ResponseEntity<ApiResponse<List<Repasse>>> listarRepasses() {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.listarRepasses()));
    }

    @GetMapping("/repasses/{id}")
    @Operation(summary = "Buscar repasse por ID")
    public ResponseEntity<ApiResponse<Repasse>> buscarRepassePorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.buscarRepassePorId(id)));
    }

    @GetMapping("/repasses/agendamento/{agendamentoId}")
    @Operation(summary = "Buscar repasses vinculados a um agendamento")
    public ResponseEntity<ApiResponse<List<Repasse>>> buscarRepassesPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.buscarRepassesPorAgendamento(agendamentoId)));
    }

    @GetMapping("/repasses/tatuador/{tatuadorId}")
    @Operation(summary = "Buscar repasses vinculados a um tatuador")
    public ResponseEntity<ApiResponse<List<Repasse>>> buscarRepassesPorTatuador(@PathVariable Long tatuadorId) {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.buscarRepassesPorTatuador(tatuadorId)));
    }

    @GetMapping("/reembolso/{agendamentoId}")
    @Operation(summary = "Simular cálculo de reembolso de cancelamento [F6-BACK-02, RN05]",
            description = "Calcula o valor reembolsável conforme antecedência: reembolso integral permitido se >= 48h, e bloqueio com taxa se < 48h.")
    public ResponseEntity<ApiResponse<CalculoReembolsoResponse>> simularReembolso(
            @PathVariable Long agendamentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataReferencia,
            @RequestParam(required = false) BigDecimal percentualDesconto) {
        CalculoReembolsoResponse response = financeiroService.calcularReembolso(agendamentoId, dataReferencia, percentualDesconto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reembolso/{agendamentoId}/cancelar")
    @Operation(summary = "Cancelar agendamento e processar reembolso conforme RN05 [F6-BACK-02]",
            description = "Cancela a sessão, bloqueando reembolso integral quando < 48h e gerando registro de reembolso no financeiro.")
    public ResponseEntity<ApiResponse<CancelamentoResponse>> cancelarAgendamento(
            @PathVariable Long agendamentoId,
            @RequestBody(required = false) CancelarAgendamentoRequest request) {
        CancelamentoResponse response = financeiroService.cancelarAgendamento(agendamentoId, request);
        return ResponseEntity.ok(ApiResponse.success("Agendamento cancelado com sucesso conforme regra RN05", response));
    }
}
