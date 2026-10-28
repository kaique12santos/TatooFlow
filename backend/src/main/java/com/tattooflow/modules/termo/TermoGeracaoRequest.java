package com.tattooflow.modules.termo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermoGeracaoRequest {

    @NotNull(message = "clienteId é obrigatório")
    private Long clienteId;

    @NotNull(message = "agendamentoId é obrigatório")
    private Long agendamentoId;

    private String ipAssinatura;

    private LocalDateTime dataAssinatura;
}

