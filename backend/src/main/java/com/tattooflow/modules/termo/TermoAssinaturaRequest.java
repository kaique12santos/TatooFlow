package com.tattooflow.modules.termo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermoAssinaturaRequest {

    private String clientId;

    private Long agendamentoId;

    private String signatureBase64;

    private String termNumber;

    private String agreedAt;

    private String botVerificationId;

    private String clientCpf;
}

