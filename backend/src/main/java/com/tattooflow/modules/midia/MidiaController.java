package com.tattooflow.modules.midia;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/midias")
public class MidiaController {

    private final MidiaService midiaService;

    public MidiaController(MidiaService midiaService) {
        this.midiaService = midiaService;
    }

    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<ApiResponse<List<Midia>>> listarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(ApiResponse.success(midiaService.listarPorAgendamento(agendamentoId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Midia>> uploadMidia(@RequestBody Midia midia) {
        return ResponseEntity.ok(ApiResponse.success("Mídia enviada com sucesso", midiaService.salvar(midia)));
    }
}
