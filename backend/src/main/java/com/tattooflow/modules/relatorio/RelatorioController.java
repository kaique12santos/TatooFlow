package com.tattooflow.modules.relatorio;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
