package com.tattooflow.modules.relatorio;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/mensal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obterRelatorioMensal(
            @RequestParam int mes,
            @RequestParam int ano) {
        return ResponseEntity.ok(ApiResponse.success(relatorioService.gerarRelatorioMensal(mes, ano)));
    }
}
