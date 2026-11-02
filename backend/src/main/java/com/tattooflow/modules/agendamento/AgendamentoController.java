package com.tattooflow.modules.agendamento;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import com.tattooflow.modules.financeiro.ComprovanteSinal;
import com.tattooflow.modules.financeiro.FinanceiroService;
import com.tattooflow.modules.financeiro.FinalizarSessaoRequest;
import com.tattooflow.modules.financeiro.FinalizarSessaoResponse;
import com.tattooflow.modules.financeiro.CancelarAgendamentoRequest;
import com.tattooflow.modules.financeiro.CancelamentoResponse;
import com.tattooflow.modules.financeiro.CalculoReembolsoResponse;
import com.tattooflow.modules.financeiro.Pagamento;
import com.tattooflow.modules.financeiro.SinalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/api/agendamentos", "/agendamentos"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Agendamentos", description = "Gestão da agenda, sinal 50% (RN01), finalização com divisão 30/70 (RN02/RN07) e cancelamento com reembolso (RN05)")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final SinalService sinalService;
    private final FinanceiroService financeiroService;

    public AgendamentoController(AgendamentoService agendamentoService,
                                 SinalService sinalService,
                                 FinanceiroService financeiroService) {
        this.agendamentoService = agendamentoService;
        this.sinalService = sinalService;
        this.financeiroService = financeiroService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos",
            description = "Retorna os agendamentos salvos no campo data, incluindo os cancelados. "
                    + "Quando não há registros, retorna uma lista vazia. Requer JWT informado em Authorize.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarTodos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<ApiResponse<Agendamento>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.buscarPorId(id)));
    }

    @GetMapping("/agenda")
    @Operation(summary = "Listar agenda filtrada por período e status [F5-BACK-01]",
            description = "Retorna os agendamentos filtrados por período (dia/semana/mês) e status.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarAgenda(
            @RequestParam(required = false, defaultValue = "dia") String periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long tatuadorId
    ) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarAgenda(periodo, data, status, tatuadorId)));
    }

    @GetMapping("/tatuador/{tatuadorId}")
    @Operation(summary = "Listar agendamentos de um tatuador",
            description = "Retorna no campo data os agendamentos do tatuador informado. "
                    + "Requer JWT informado em Authorize.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarPorTatuador(@PathVariable Long tatuadorId) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarPorTatuador(tatuadorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Agendamento>> criar(@RequestBody Agendamento agendamento) {
        return ResponseEntity.ok(ApiResponse.success("Agendamento criado com sucesso", agendamentoService.salvar(agendamento)));
    }

    @PostMapping(value = "/{id}/sinal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrar sinal de 50% e confirmar o agendamento",
            description = "Recebe o valor do sinal e um comprovante PDF, PNG ou JPEG de até 5 MB. Requer JWT.")
    public ResponseEntity<ApiResponse<Pagamento>> registrarSinal(@PathVariable Long id,
                                                                 @RequestParam BigDecimal valor,
                                                                 @RequestParam(defaultValue = "PIX") String formaPagamento,
                                                                 @RequestPart MultipartFile comprovante) {
        Pagamento sinal = sinalService.registrar(id, valor, formaPagamento, comprovante);
        return ResponseEntity.status(201).body(ApiResponse.success("Sinal registrado e agendamento confirmado", sinal));
    }

    @GetMapping("/{id}/sinal/comprovante")
    @Operation(summary = "Baixar comprovante do sinal", description = "Retorna o arquivo registrado. Requer JWT.")
    public ResponseEntity<byte[]> baixarComprovanteSinal(@PathVariable Long id) {
        ComprovanteSinal comprovante = sinalService.buscarComprovante(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(comprovante.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(comprovante.nome(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(comprovante.conteudo());
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar sessão de agendamento com cálculo de divisão 30/70 [F6-BACK-01, RN02, RN07]",
            description = "Conclui a sessão e calcula automaticamente a divisão 30% estúdio / 70% artista deduzindo insumos.")
    public ResponseEntity<ApiResponse<FinalizarSessaoResponse>> finalizarSessao(
            @PathVariable Long id,
            @RequestBody(required = false) FinalizarSessaoRequest request) {
        BigDecimal insumos = request != null ? request.getCustoInsumos() : null;
        BigDecimal valorTotal = request != null ? request.getValorTotal() : null;
        FinalizarSessaoResponse response = financeiroService.finalizarSessao(id, insumos, valorTotal);
        return ResponseEntity.ok(ApiResponse.success("Sessão finalizada com sucesso e repasse apurado", response));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar agendamento aplicando política de reembolso RN05 [F6-BACK-02]",
            description = "Cancela o agendamento e aplica regra de retenção se < 48h ou reembolso integral se >= 48h.")
    public ResponseEntity<ApiResponse<CancelamentoResponse>> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) CancelarAgendamentoRequest request) {
        CancelamentoResponse response = financeiroService.cancelarAgendamento(id, request);
        return ResponseEntity.ok(ApiResponse.success("Agendamento cancelado com sucesso conforme regra RN05", response));
    }

    @GetMapping("/{id}/reembolso")
    @Operation(summary = "Consultar valor reembolsável em caso de cancelamento [F6-BACK-02, RN05]",
            description = "Simula a política de cancelamento informando se há direito a reembolso integral ou desconto/retenção.")
    public ResponseEntity<ApiResponse<CalculoReembolsoResponse>> consultarReembolso(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataReferencia,
            @RequestParam(required = false) BigDecimal percentualDesconto) {
        CalculoReembolsoResponse response = financeiroService.calcularReembolso(id, dataReferencia, percentualDesconto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
