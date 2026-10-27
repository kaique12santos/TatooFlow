package com.tattooflow.modules.agendamento;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import com.tattooflow.modules.financeiro.SinalService;
import com.tattooflow.modules.financeiro.Pagamento;
import com.tattooflow.modules.financeiro.ComprovanteSinal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final SinalService sinalService;

    public AgendamentoController(AgendamentoService agendamentoService, SinalService sinalService) {
        this.agendamentoService = agendamentoService;
        this.sinalService = sinalService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos",
            description = "Retorna os agendamentos salvos no campo data, incluindo os cancelados. "
                    + "Quando não há registros, retorna uma lista vazia. Requer JWT informado em Authorize.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarTodos()));
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
}
