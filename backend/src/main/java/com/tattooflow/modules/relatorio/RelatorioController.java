package com.tattooflow.modules.relatorio;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping({"/api/relatorios", "/relatorios"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Relatórios", description = "Geração de relatórios consolidados mensais de faturamento, comissões e gastos com insumos (F8-BACK-01)")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/mensal")
    @Operation(summary = "Gerar relatório financeiro e operacional consolidado do mês [F8-BACK-01]",
            description = "Consolida faturamento bruto, repasses a artistas, comissões do estúdio (30%), gastos com insumos deduzidos e métricas de estoque.")
    public ResponseEntity<ApiResponse<RelatorioMensalDTO>> obterRelatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        return ResponseEntity.ok(ApiResponse.success("Relatório mensal gerado com sucesso", relatorioService.gerarRelatorioMensal(mes, ano)));
    }

    @GetMapping(value = "/mensal/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exportar relatório financeiro e operacional mensal em PDF",
            description = "Gera e exporta em PDF o fechamento consolidado mensal, livro-razão notarial e detalhamento de repasses.")
    public ResponseEntity<byte[]> baixarRelatorioMensalPdf(
            @RequestParam(name = "mes", required = false) String mesParam,
            @RequestParam(name = "ano", required = false) Integer anoParam) {

        int[] mesAno = resolverMesEAno(mesParam, anoParam);
        int mes = mesAno[0];
        int ano = mesAno[1];

        byte[] pdfBytes = relatorioService.gerarRelatorioMensalPdf(mes, ano);
        String nomeArquivo = String.format("relatorio_fechamento_%02d_%04d.pdf", mes, ano);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nomeArquivo, StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(pdfBytes);
    }

    private int[] resolverMesEAno(String mesParam, Integer anoParam) {
        int mes = YearMonth.now().getMonthValue();
        int ano = anoParam != null ? anoParam : YearMonth.now().getYear();

        if (mesParam != null && !mesParam.isBlank()) {
            String trimmed = mesParam.trim();
            if (trimmed.contains("/")) {
                String[] parts = trimmed.split("/");
                String mPart = parts[0].trim().toUpperCase(Locale.ROOT);
                String yPart = parts[1].trim();
                try {
                    ano = Integer.parseInt(yPart.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
                mes = parseNomeMesOuNumero(mPart);
            } else {
                mes = parseNomeMesOuNumero(trimmed.toUpperCase(Locale.ROOT));
            }
        }
        if (anoParam != null) {
            ano = anoParam;
        }
        return new int[]{mes, ano};
    }

    private int parseNomeMesOuNumero(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        Map<String, Integer> map = Map.ofEntries(
                Map.entry("JANEIRO", 1),
                Map.entry("FEVEREIRO", 2),
                Map.entry("MARÇO", 3),
                Map.entry("MARCO", 3),
                Map.entry("ABRIL", 4),
                Map.entry("MAIO", 5),
                Map.entry("JUNHO", 6),
                Map.entry("JULHO", 7),
                Map.entry("AGOSTO", 8),
                Map.entry("SETEMBRO", 9),
                Map.entry("OUTUBRO", 10),
                Map.entry("NOVEMBRO", 11),
                Map.entry("DEZEMBRO", 12)
        );
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (str.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return YearMonth.now().getMonthValue();
    }
}
