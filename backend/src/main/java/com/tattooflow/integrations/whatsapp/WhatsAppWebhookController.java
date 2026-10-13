package com.tattooflow.integrations.whatsapp;

import com.tattooflow.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);
    private final WhatsAppProperties properties;
    private final WhatsAppWebhookVerification verification;

    public WhatsAppWebhookController(WhatsAppProperties properties, WhatsAppWebhookVerification verification) {
        this.properties = properties;
        this.verification = verification;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if (!properties.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("WhatsApp desabilitado");
        }
        if (!"subscribe".equals(mode) || !StringUtils.hasText(challenge)) {
            return ResponseEntity.badRequest().body("Verificacao incompleta ou modo invalido");
        }
        if (!verification.isVerifyTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token de verificacao invalido");
        }
        log.info("Verificacao do webhook WhatsApp concluida");
        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> receberWebhook(
            @RequestBody byte[] payload,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature) {
        if (!properties.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("WhatsApp desabilitado"));
        }
        if (!verification.isSignatureValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Assinatura do webhook invalida"));
        }
        // F2-INFRA-01: autentica e confirma o evento de teste, sem executar triagem.
        log.info("Evento de teste do webhook WhatsApp autenticado e recebido");
        return ResponseEntity.ok(ApiResponse.success("Webhook recebido com sucesso", null));
    }
}
