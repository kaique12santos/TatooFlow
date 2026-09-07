package com.tattooflow.modules.estoque;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemEstoque>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemEstoque>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(estoqueService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ItemEstoque>> salvar(@RequestBody ItemEstoque item) {
        return ResponseEntity.ok(ApiResponse.success("Item de estoque salvo", estoqueService.salvar(item)));
    }
}
