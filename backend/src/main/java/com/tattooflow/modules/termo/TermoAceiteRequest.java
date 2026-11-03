package com.tattooflow.modules.termo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermoAceiteRequest {

    private String ipAssinatura;

    private String assinaturaBase64;

    private String documentoCliente;

    private String observacoes;

    private LocalDateTime dataAssinatura;
}

