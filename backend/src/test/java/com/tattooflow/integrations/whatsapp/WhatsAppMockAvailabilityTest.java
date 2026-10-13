package com.tattooflow.integrations.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppMockAvailabilityTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withUserConfiguration(WhatsAppMockController.class, WhatsAppMockService.class);

    @Test
    void devePermanecerDesabilitadoPorPadraoMesmoEmDesenvolvimento() {
        contextRunner.withInitializer(context -> context.getEnvironment().setActiveProfiles("dev"))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(WhatsAppMockController.class)
                            .doesNotHaveBean(WhatsAppMockService.class);
                });
    }

    @ParameterizedTest
    @CsvSource({
            "dev,true,true",
            "h2,true,true",
            "dev,false,false",
            "h2,false,false",
            "prod,true,false",
            "dev;prod,true,false",
            "h2;prod,true,false",
            "staging,true,false"
    })
    void deveHabilitarSomenteComFlagEPerfilLocalSemProducao(String profiles, boolean enabled, boolean expected) {
        contextRunner.withInitializer(context -> context.getEnvironment().setActiveProfiles(profiles.split(";")))
                .withPropertyValues("integrations.whatsapp.mock.enabled=" + enabled)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    if (expected) {
                        assertThat(context).hasSingleBean(WhatsAppMockController.class)
                                .hasSingleBean(WhatsAppMockService.class);
                    } else {
                        assertThat(context).doesNotHaveBean(WhatsAppMockController.class)
                                .doesNotHaveBean(WhatsAppMockService.class);
                    }
                });
    }
}
