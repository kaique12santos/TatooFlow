package com.tattooflow.modules.agendamento;

import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para listagem de agenda por período (dia/semana/mês) e status [F5-BACK-01].
 */
@RestController
@RequestMapping({"/api/agenda", "/agenda"})
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@Tag(name = "Agenda", description = "Endpoints para consulta da agenda filtrada por período e status [F5-BACK-01]")
public class AgendaController {

    private final AgendamentoService agendamentoService;

    public AgendaController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar agenda por período (dia/semana/mês) [F5-BACK-01]",
            description = "Consulta a agenda do tatuador filtrando por período (dia, semana ou mês), "
                    + "com suporte a filtro por status e data de referência. Resposta rápida conforme RNF08."
    )
    public ResponseEntity<ApiResponse<List<Agendamento>>> listarAgenda(
            @Parameter(description = "Período da consulta: dia, semana ou mes (padrão: dia)")
            @RequestParam(required = false, defaultValue = "dia") String periodo,

            @Parameter(description = "Data de referência no formato YYYY-MM-DD (padrão: data atual)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,

            @Parameter(description = "Filtro opcional por status do agendamento (ex: AGENDADO, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO)")
            @RequestParam(required = false) String status,

            @Parameter(description = "Filtro opcional por ID do tatuador")
            @RequestParam(required = false) Long tatuadorId
    ) {
        List<Agendamento> agendamentos = agendamentoService.listarAgenda(periodo, data, status, tatuadorId);
        return ResponseEntity.ok(ApiResponse.success(agendamentos));
    }
}

