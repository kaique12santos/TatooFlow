package com.tattooflow.modules.termo;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/termos")
public class TermoController {

    private final TermoService termoService;

    public TermoController(TermoService termoService) {
        this.termoService = termoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TermoAceite>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(termoService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TermoAceite>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(termoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TermoAceite>> registrar(@RequestBody TermoAceite termo) {
        return ResponseEntity.ok(ApiResponse.success("Termo assinado registrado", termoService.salvar(termo)));
    }
}
