package com.tattooflow.modules.notificacao;

import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NotificacaoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(NotificacaoProperties.class, NotificacaoConfig.class, NotificacaoScheduler.class)
            .withBean(AgendamentoRepository.class, () -> mock(AgendamentoRepository.class))
            .withBean(NotificacaoService.class, () -> mock(NotificacaoService.class));

    @Test
    void deveRegistrarDoisJobsComConfiguracaoPadrao() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).hasSize(2);
            assertThat(context.getBean(NotificacaoProperties.class).getPreHoras()).isEqualTo(24);
        });
    }

    @Test
    void devePermitirDesativarJobs() {
        runner.withPropertyValues("notificacoes.cuidados.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(NotificacaoScheduler.class);
            assertThat(context.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks()).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "pre-horas=0", "pos-horas=-1", "intervalo-ms=0", "zona=Invalida",
            "template-pre=", "template-pos=", "whatsapp-template-pre=", "whatsapp-idioma="
    })
    void deveRejeitarConfiguracaoInvalida(String propriedade) {
        runner.withPropertyValues("notificacoes.cuidados." + propriedade)
                .run(context -> assertThat(context).hasFailed());
    }
}
