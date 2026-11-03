package com.tattooflow.integrations.whatsapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(WhatsAppProperties.class);

    @Test
    void devePermitirAplicacaoSemCredenciaisComIntegracaoDesabilitada() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            WhatsAppProperties properties = context.getBean(WhatsAppProperties.class);
            WhatsAppWebhookController controller = new WhatsAppWebhookController(properties,
                    new WhatsAppWebhookVerification(properties));
            assertThat(controller.verificarWebhook("subscribe", "", "123").getStatusCode())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(controller.receberWebhook(new byte[0], null).getStatusCode())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        });
    }

    @Test
    void deveImpedirInicializacaoHabilitadaSemCredenciais() {
        contextRunner.withPropertyValues("integrations.whatsapp.enabled=true")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void deveRejeitarVersaoInvalidaSemExporSegredosNaValidacao() {
        contextRunner.withPropertyValues(
                        "integrations.whatsapp.enabled=true",
                        "integrations.whatsapp.verify-token=private-verify-token",
                        "integrations.whatsapp.app-secret=private-app-secret",
                        "integrations.whatsapp.access-token=private-access-token",
                        "integrations.whatsapp.phone-number-id=123",
                        "integrations.whatsapp.business-account-id=456",
                        "integrations.whatsapp.graph-api-version=invalid")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("graph-api-version")
                            .hasStackTraceContaining("vNN.N");
                    StringWriter failure = new StringWriter();
                    context.getStartupFailure().printStackTrace(new PrintWriter(failure));
                    assertThat(failure.toString())
                            .doesNotContain("private-verify-token", "private-app-secret", "private-access-token");
                });
    }
}
