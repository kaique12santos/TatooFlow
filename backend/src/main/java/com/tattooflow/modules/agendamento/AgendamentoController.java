package com.tattooflow.modules.agendamento;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos",
            description = "Retorna os agendamentos salvos no campo data, incluindo os cancelados. "
                    + "Quando não há registros, retorna uma lista vazia. Requer JWT informado em Authorize.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarTodos()));
    }

    @GetMapping("/tatuador/{tatuadorId}")
    @Operation(summary = "Listar agendamentos de um tatuador",
            description = "Retorna no campo data os agendamentos do tatuador informado. "
                    + "Requer JWT informado em Authorize.")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarPorTatuador(@PathVariable Long tatuadorId) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarPorTatuador(tatuadorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Agendamento>> criar(@RequestBody Agendamento agendamento) {
        return ResponseEntity.ok(ApiResponse.success("Agendamento criado com sucesso", agendamentoService.salvar(agendamento)));
    }
}
