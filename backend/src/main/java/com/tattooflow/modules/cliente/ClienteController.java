package com.tattooflow.modules.cliente;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import com.tattooflow.modules.termo.TermoAceite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/clientes", "/clientes"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Clientes", description = "Gestão cadastral, busca e histórico do cliente")
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

    @GetMapping("/busca")
    @Operation(
            summary = "Buscar/filtrar clientes e orçamentos por nome ou tipo de trabalho [F5-BACK-02, UC04.1]",
            description = "Filtra clientes por nome ou tipo de trabalho/estilo registrado em seus orçamentos (pré-agendamentos) "
                    + "ou sessões (agendamentos). Resposta rápida dentro de 2-3s (RNF08)."
    )
    public ResponseEntity<ApiResponse<List<ClienteBuscaResponse>>> buscarClientes(
            @Parameter(description = "Termo de busca (nome do cliente ou tipo/estilo de trabalho)")
            @RequestParam(name = "termo", required = false) String termo) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarClientes(termo)));
    }

    @GetMapping("/{id}/historico")
    @Operation(
            summary = "Consultar histórico completo do cliente [F5-BACK-03]",
            description = "Retorna o histórico com todas as sessões (agendamentos), termos de consentimento assinados "
                    + "e fotos/mídias vinculadas ao cliente."
    )
    public ResponseEntity<ApiResponse<ClienteHistoricoResponse>> buscarHistoricoCompleto(
            @Parameter(description = "Identificador do cliente")
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clienteService.buscarHistoricoCompleto(id)));
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
