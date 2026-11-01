package com.tattooflow.modules.postagem;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para solicitação de agendamento de postagem no Instagram [F4-BACK-03, UC03.2].
 * Suporta tanto as chamadas nativas do backend quanto o payload da store frontend (useMarketingStore).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgendarPostagemRequest {

    private Long agendamentoId;
    private Long midiaId;

    @JsonAlias({"masterShotUrl", "urlImagem", "url"})
    private String imageUrl;

    @JsonAlias({"caption", "texto", "descricao"})
    private String legenda;

    @JsonAlias({"dataHora", "scheduledAt"})
    private LocalDateTime dataHoraAgendada;

    // Suporte aos campos da tela de distribuição do frontend
    private String scheduleDate; // ex: "23/MAI/2025" ou "2026-11-25"
    private String scheduleTime; // ex: "21:30"

    @JsonAlias({"canais"})
    private Map<String, Boolean> channels;

    @JsonAlias({"account_id", "activeAccountId"})
    private String accountId;

    private Map<String, Object> client;
}

