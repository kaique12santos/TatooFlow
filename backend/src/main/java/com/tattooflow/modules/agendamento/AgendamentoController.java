package com.tattooflow.modules.agendamento;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarTodos()));
    }

    @GetMapping("/tatuador/{tatuadorId}")
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarPorTatuador(@PathVariable Long tatuadorId) {
        return ResponseEntity.ok(ApiResponse.success(agendamentoService.listarPorTatuador(tatuadorId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Agendamento>> criar(@RequestBody Agendamento agendamento) {
        return ResponseEntity.ok(ApiResponse.success("Agendamento criado com sucesso", agendamentoService.salvar(agendamento)));
    }
}
