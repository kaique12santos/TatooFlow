package com.tattooflow.modules.termo;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping({"/api/termos", "/termos"})
@Tag(name = "Termos de Consentimento", description = "Gestão, aceite digital e geração de PDF do termo de consentimento (RN04, RES02, RN06)")
public class TermoController {

    private final TermoService termoService;
    private final TermoStorageService termoStorageService;

    public TermoController(TermoService termoService, TermoStorageService termoStorageService) {
        this.termoService = termoService;
        this.termoStorageService = termoStorageService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os termos cadastrados")
    public ResponseEntity<ApiResponse<List<TermoAceite>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(termoService.listarTodos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar termo de consentimento por ID")
    public ResponseEntity<ApiResponse<TermoAceite>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(termoService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Registrar termo assinado existente")
    public ResponseEntity<ApiResponse<TermoAceite>> registrar(@RequestBody TermoAceite termo) {
        return ResponseEntity.ok(ApiResponse.success("Termo assinado registrado", termoService.salvar(termo)));
    }

    /**
     * Endpoint de aceite digital do cliente [F3-BACK-02]
     * Acessível tanto em POST /termos/{id}/aceitar quanto em POST /api/termos/{id}/aceitar.
     */
    @PostMapping("/{id}/aceitar")
    @Operation(summary = "Aceitar digitalmente o termo de consentimento pelo cliente [F3-BACK-02]")
    public ResponseEntity<ApiResponse<TermoAceite>> aceitarTermo(
            @PathVariable Long id,
            @RequestBody(required = false) TermoAceiteRequest request,
            HttpServletRequest servletRequest) {

        String ip = extrairIp(servletRequest);
        TermoAceite aceito = termoService.aceitarTermo(id, request, ip);
        return ResponseEntity.ok(ApiResponse.success("Termo de consentimento aceito digitalmente com sucesso!", aceito));
    }

    /**
     * Vínculo do termo ao histórico do cliente [F3-BACK-02]
     */
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Consultar histórico de termos vinculados ao cliente [F3-BACK-02]")
    public ResponseEntity<ApiResponse<List<TermoAceite>>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ApiResponse.success(termoService.listarPorCliente(clienteId)));
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar e arquivar termo de consentimento em PDF com dados do cliente e data/hora do aceite (RN04)")
    public ResponseEntity<ApiResponse<TermoAceite>> gerarTermoComPdf(
            @Valid @RequestBody TermoGeracaoRequest request,
            HttpServletRequest servletRequest) {

        String ip = request.getIpAssinatura() != null && !request.getIpAssinatura().isBlank()
                ? request.getIpAssinatura()
                : extrairIp(servletRequest);

        TermoAceite criado = termoService.registrarComPdf(
                request.getClienteId(),
                request.getAgendamentoId(),
                ip,
                request.getDataAssinatura()
        );

        return ResponseEntity.ok(ApiResponse.success("Termo gerado e armazenado com sucesso", criado));
    }

    @PostMapping("/{id}/gerar-pdf")
    @Operation(summary = "Gerar ou regenerar PDF do termo e armazenar com segurança")
    public ResponseEntity<ApiResponse<TermoAceite>> gerarPdfParaTermo(@PathVariable Long id) {
        TermoAceite atualizado = termoService.gerarEArmazenarPdf(id);
        return ResponseEntity.ok(ApiResponse.success("PDF do termo gerado e armazenado com segurança", atualizado));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Baixar documento PDF do termo de consentimento (RN04, RES02)")
    public ResponseEntity<byte[]> baixarPdf(@PathVariable Long id) {
        byte[] pdfBytes = termoService.obterPdf(id);
        String hash = termoStorageService.calcularSha256(pdfBytes);

        String nomeArquivo = String.format("termo_consentimento_%d.pdf", id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nomeArquivo, StandardCharsets.UTF_8).build().toString())
                .header("X-Termo-Integridade-SHA256", hash)
                .body(pdfBytes);
    }

    @PostMapping("/assinar")
    @Operation(summary = "Autenticar e assinar termo notarial de consentimento com geração de PDF")
    public ResponseEntity<ApiResponse<TermoAssinaturaResponse>> assinarTermo(
            @RequestBody TermoAssinaturaRequest payload,
            HttpServletRequest servletRequest) {

        Long clienteId = extrairClienteId(payload.getClientId());
        Long agendamentoId = payload.getAgendamentoId() != null ? payload.getAgendamentoId() : 1L;

        LocalDateTime dataAssinatura = parseDataAssinatura(payload.getAgreedAt());
        String ip = extrairIp(servletRequest);

        TermoAceite termo = termoService.registrarComPdf(clienteId, agendamentoId, ip, dataAssinatura);

        String protocolo = payload.getTermNumber() != null && !payload.getTermNumber().isBlank()
                ? payload.getTermNumber()
                : String.format("TERM-%d", termo.getId());

        TermoAssinaturaResponse response = TermoAssinaturaResponse.builder()
                .success(true)
                .fromBackend(true)
                .message("Termo de consentimento autenticado, PDF gerado e arquivado com sucesso no servidor!")
                .protocol(protocolo)
                .pdfUrl("/api/termos/" + termo.getId() + "/pdf")
                .termoId(termo.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Termo assinado e autenticado", response));
    }

    private Long extrairClienteId(String rawClientId) {
        if (rawClientId == null || rawClientId.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(rawClientId.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 1L;
        }
    }

    private LocalDateTime parseDataAssinatura(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(raw.replace("Z", ""));
            } catch (Exception ignored) {
                return LocalDateTime.now();
            }
        }
    }

    private String extrairIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "127.0.0.1";
    }
}
