package com.tattooflow.modules.notificacao;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Component
@ConfigurationProperties(prefix = "notificacoes.cuidados")
@Validated
@Getter
@Setter
public class NotificacaoProperties {

    private boolean enabled = true;
    @Min(1)
    private long preHoras = 24;
    @Min(0)
    private long posHoras = 2;
    @Min(1)
    private long intervaloMs = 60000;
    private ZoneId zona = ZoneId.of("America/Sao_Paulo");
    @NotBlank
    private String templatePre = "Olá, {nome}! Sua sessão será em {data} às {hora}. "
            + "Revise os cuidados pré-sessão informados pelo estúdio e entre em contato para esclarecer dúvidas.";
    @NotBlank
    private String templatePos = "Olá, {nome}! Após sua sessão de {data} às {hora}, "
            + "siga as orientações de cuidados entregues pelo seu tatuador. Em caso de dúvidas, entre em contato com o estúdio.";
    @NotBlank
    private String whatsappTemplatePre = "cuidados_pre_sessao";
    @NotBlank
    private String whatsappTemplatePos = "cuidados_pos_sessao";
    @NotBlank
    private String whatsappIdioma = "pt_BR";
}
