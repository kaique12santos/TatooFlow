package com.tattooflow.integrations.whatsapp;

import com.tattooflow.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp/mock")
@Profile("!prod & (dev | h2)")
@ConditionalOnProperty(prefix = "integrations.whatsapp.mock", name = "enabled", havingValue = "true")
public class WhatsAppMockController {

    private final WhatsAppMockService mockService;

    public WhatsAppMockController(WhatsAppMockService mockService) {
        this.mockService = mockService;
    }

    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<WhatsAppMockMessageReceivedEvent>> simularMensagemRecebida(
            @Valid @RequestBody SimularMensagemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Mensagem recebida em modo MOCK (simulacao local)",
                mockService.simularRecebimentoMensagem(request)));
    }
}
