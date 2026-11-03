package com.tattooflow.modules.notificacao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
public class NotificacaoConfig {

    @Bean
    public Clock notificacaoClock(NotificacaoProperties properties) {
        return Clock.system(properties.getZona());
    }
}
