package com.tattooflow.modules.cliente;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import com.tattooflow.modules.termo.TermoAceite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Clientes", description = "Gestão cadastral e histórico do cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<ApiResponse<List<Cliente>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(clienteService.listarTodos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ApiResponse<Cliente>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente")
    public ResponseEntity<ApiResponse<Cliente>> criar(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(ApiResponse.success("Cliente cadastrado com sucesso", clienteService.salvar(cliente)));
    }

    @GetMapping("/{id}/termos")
    @Operation(summary = "Consultar histórico de termos de aceite do cliente [F3-BACK-02]")
    public ResponseEntity<ApiResponse<List<TermoAceite>>> buscarTermosDoCliente(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarHistoricoTermos(id)));
    }

    @GetMapping("/{id}/historico-termos")
    @Operation(summary = "Consultar histórico de termos de aceite do cliente (alias)")
    public ResponseEntity<ApiResponse<List<TermoAceite>>> buscarHistoricoTermosDoCliente(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarHistoricoTermos(id)));
    }
}
