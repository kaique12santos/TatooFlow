package com.tattooflow.integrations.whatsapp;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WhatsAppWebhookVerification {

    private final WhatsAppProperties properties;

    public WhatsAppWebhookVerification(WhatsAppProperties properties) {
        this.properties = properties;
    }

    public boolean isVerifyTokenValid(String token) {
        return properties.isEnabled() && token != null
                && MessageDigest.isEqual(properties.getVerifyToken().getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isSignatureValid(byte[] payload, String signature) {
        if (!properties.isEnabled() || signature == null
                || !signature.matches("sha256=[0-9a-fA-F]{64}")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getAppSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return MessageDigest.isEqual(mac.doFinal(payload),
                    HexFormat.of().parseHex(signature.substring("sha256=".length())));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Falha ao verificar assinatura do webhook WhatsApp", ex);
        }
    }
}
