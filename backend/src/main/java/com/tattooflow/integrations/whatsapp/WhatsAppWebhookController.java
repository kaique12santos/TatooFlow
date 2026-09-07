package com.tattooflow.integrations.whatsapp;

import com.tattooflow.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

    @PostMapping
    public ResponseEntity<ApiResponse<String>> receberWebhook(@RequestBody String payload) {
        // Recebimento de mensagens e status do WhatsApp
        return ResponseEntity.ok(ApiResponse.success("Webhook recebido com sucesso", null));
    }
}
