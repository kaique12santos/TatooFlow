package com.tattooflow.modules.cliente;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cliente>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(clienteService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cliente>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Cliente>> criar(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(ApiResponse.success("Cliente cadastrado com sucesso", clienteService.salvar(cliente)));
    }
}
