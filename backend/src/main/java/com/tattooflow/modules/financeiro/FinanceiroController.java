package com.tattooflow.modules.financeiro;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/pagamentos")
    public ResponseEntity<ApiResponse<List<Pagamento>>> listarPagamentos() {
        return ResponseEntity.ok(ApiResponse.success(financeiroService.listarPagamentos()));
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<ApiResponse<Pagamento>> registrarPagamento(@RequestBody Pagamento pagamento) {
        return ResponseEntity.ok(ApiResponse.success("Pagamento registrado", financeiroService.registrarPagamento(pagamento)));
    }
}
