package com.tattooflow.integrations.whatsapp;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "integrations.whatsapp")
@Validated
@Getter
@Setter
public class WhatsAppProperties {

    private boolean enabled;
    private String verifyToken;
    private String appSecret;
    private String accessToken;
    private String phoneNumberId;
    private String businessAccountId;
    private String graphApiVersion;

    @AssertTrue(message = "WhatsApp habilitado exige verify-token, app-secret, access-token, "
            + "phone-number-id, business-account-id e graph-api-version no formato vNN.N")
    public boolean isConfigurationValid() {
        return !enabled || (StringUtils.hasText(verifyToken)
                && StringUtils.hasText(appSecret)
                && StringUtils.hasText(accessToken)
                && StringUtils.hasText(phoneNumberId)
                && StringUtils.hasText(businessAccountId)
                && graphApiVersion != null && graphApiVersion.matches("v\\d+\\.\\d+"));
    }
}
