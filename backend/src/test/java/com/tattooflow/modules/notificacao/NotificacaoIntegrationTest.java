package com.tattooflow.modules.notificacao;

import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.cliente.Cliente;
import com.tattooflow.modules.cliente.ClienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notificacao_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.jpa.show-sql=false",
        "notificacoes.cuidados.enabled=false",
        "notificacoes.cuidados.template-pre=Pré {nome}: {data} às {hora}",
        "notificacoes.cuidados.template-pos=Pós {nome}: {data} às {hora}"
})
@ActiveProfiles("h2")
class NotificacaoIntegrationTest {
    @TestConfiguration
    static class TempoConfig {
        @Bean
        @Primary
        Clock tempoFixo() {
            return Clock.fixed(Instant.parse("2026-10-22T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Autowired private AgendamentoRepository agendamentos;
    @Autowired private ClienteRepository clientes;
    @Autowired private NotificacaoEnvioRepository envios;
    @Autowired private NotificacaoService service;
    @Autowired private NotificacaoProperties properties;
    @Autowired private Clock clock;
    @MockBean private WhatsAppMessageSender sender;

    private NotificacaoScheduler scheduler;
    private Cliente cliente;
    private LocalDateTime agora;

    @BeforeEach
    void preparar() {
        limpar();
        properties.setPreHoras(24);
        properties.setPosHoras(2);
        agora = LocalDateTime.now(clock);
        cliente = clientes.saveAndFlush(Cliente.builder().nome("Ana").telefone("5511999999999").build());
        scheduler = new NotificacaoScheduler(agendamentos, service, properties, clock);
    }

    @AfterEach
    void limpar() {
        envios.deleteAll();
        agendamentos.deleteAll();
        clientes.deleteAll();
    }

    @Test
    void deveEnviarPreNoLimiteComTemplateConfiguradoSemRepetir() {
        Agendamento agendamento = salvar(agora.plusHours(24), 90, "AGENDADO");
        scheduler.agendarNotificacoesPreSessao();
        scheduler.agendarNotificacoesPreSessao();
        // Nova chamada ao serviço (como outro processo com uma seleção antiga).
        service.enviarNotificacaoCuidadosPreSessao(agendamento.getId());

        verify(sender).enviarTemplate("5511999999999", "cuidados_pre_sessao", "pt_BR",
                List.of("Ana", "23/10/2026", "12:00"), "Pré Ana: 23/10/2026 às 12:00");
        assertThat(envios.findAll()).singleElement().satisfies(envio -> {
            assertThat(envio.getTipo()).isEqualTo(TipoNotificacao.PRE_SESSAO);
            assertThat(envio.getAgendamentoId()).isEqualTo(agendamento.getId());
            assertThat(envio.getEnviadoEm()).isEqualTo(agora);
        });
    }

    @Test
    void deveRespeitarJanelaPreSemEnviarParaSessoesIniciadasOuDistantes() {
        salvar(agora.plusHours(24).plusSeconds(1), 60, "AGENDADO");
        salvar(agora, 60, "AGENDADO");
        salvar(agora.minusHours(1), 60, "AGENDADO");
        scheduler.agendarNotificacoesPreSessao();
        verifyNoInteractions(sender);
        assertThat(envios.count()).isZero();
    }

    @Test
    void deveUsarPrazosConfiguradosERecuperarPreAtrasadoAntesDoInicio() {
        properties.setPreHoras(6);
        salvar(agora.plusHours(7), 60, "AGENDADO");
        salvar(agora.plusHours(1), 60, "AGENDADO");
        scheduler.agendarNotificacoesPreSessao();
        verify(sender).enviarTemplate(anyString(), eq("cuidados_pre_sessao"), anyString(),
                eq(List.of("Ana", "22/10/2026", "13:00")), anyString());
        assertThat(envios.count()).isEqualTo(1);
    }

    @Test
    void deveEnviarPosNoLimiteAposTerminoENaoRepetir() {
        salvar(agora.minusHours(4), 120, "CONCLUIDO");
        salvar(agora.minusHours(4).plusSeconds(1), 120, "CONCLUIDO");
        scheduler.agendarNotificacoesPosSessao();
        scheduler.agendarNotificacoesPosSessao();
        verify(sender).enviarTemplate("5511999999999", "cuidados_pos_sessao", "pt_BR",
                List.of("Ana", "22/10/2026", "08:00"), "Pós Ana: 22/10/2026 às 08:00");
        assertThat(envios.findAll()).singleElement()
                .extracting(NotificacaoEnvio::getTipo).isEqualTo(TipoNotificacao.POS_SESSAO);
    }

    @Test
    void deveRecuperarPosAtrasadoComPrazoConfigurado() {
        properties.setPosHoras(5);
        salvar(agora.minusDays(2), 60, "CONCLUIDO");
        salvar(agora.minusHours(5), 60, "CONCLUIDO");
        scheduler.agendarNotificacoesPosSessao();
        verify(sender).enviarTemplate(anyString(), eq("cuidados_pos_sessao"), anyString(),
                eq(List.of("Ana", "20/10/2026", "12:00")), anyString());
        assertThat(envios.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELADO", "EM_ANDAMENTO", "CONCLUIDO"})
    void naoDeveEnviarPreParaStatusInelegivel(String status) {
        Agendamento agendamento = salvar(agora.plusHours(1), 60, status);
        scheduler.agendarNotificacoesPreSessao();
        service.enviarNotificacaoCuidadosPreSessao(agendamento.getId());
        verifyNoInteractions(sender);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CANCELADO", "EM_ANDAMENTO", "AGENDADO"})
    void naoDeveEnviarPosParaStatusInelegivel(String status) {
        Agendamento agendamento = salvar(agora.minusDays(1), 60, status);
        scheduler.agendarNotificacoesPosSessao();
        service.enviarNotificacaoCuidadosPosSessao(agendamento.getId());
        verifyNoInteractions(sender);
    }

    @Test
    void deveRevalidarAgendamentoCanceladoOuRemarcadoAposSelecao() {
        Agendamento cancelado = salvar(agora.plusHours(1), 60, "AGENDADO");
        Agendamento remarcado = salvar(agora.plusHours(2), 60, "AGENDADO");
        List<Long> selecionados = agendamentos.buscarPendentesPreSessao(agora, agora.plusHours(24));
        cancelado.setStatus("CANCELADO");
        remarcado.setDataHora(agora.plusDays(3));
        agendamentos.saveAllAndFlush(List.of(cancelado, remarcado));
        selecionados.forEach(service::enviarNotificacaoCuidadosPreSessao);
        verifyNoInteractions(sender);
    }

    @Test
    void falhaNaoDeveMarcarEnvioNemInterromperOutrosClientesEDeveTentarNovamente() {
        salvar(agora.plusHours(1), 60, "AGENDADO");
        salvar(agora.plusHours(2), 60, "AGENDADO");
        doThrow(new IllegalStateException("Provedor indisponível")).doNothing()
                .when(sender).enviarTemplate(anyString(), anyString(), anyString(), anyList(), anyString());

        scheduler.agendarNotificacoesPreSessao();
        assertThat(envios.count()).isEqualTo(1);
        scheduler.agendarNotificacoesPreSessao();
        assertThat(envios.count()).isEqualTo(2);
        verify(sender, times(3)).enviarTemplate(anyString(), anyString(), anyString(), anyList(), anyString());
    }

    @Test
    void clienteSemTelefoneNaoDeveSerMarcadoENaoDeveBloquearOutros() {
        cliente.setTelefone(" ");
        clientes.saveAndFlush(cliente);
        salvar(agora.plusHours(1), 60, "AGENDADO");
        scheduler.agendarNotificacoesPreSessao();
        verifyNoInteractions(sender);
        assertThat(envios.count()).isZero();

        cliente.setTelefone("5511888888888");
        clientes.saveAndFlush(cliente);
        scheduler.agendarNotificacoesPreSessao();
        assertThat(envios.count()).isEqualTo(1);
    }

    @Test
    void devePermitirUmPreEUmPosParaOMesmoAgendamento() {
        Agendamento agendamento = salvar(agora.plusHours(1), 60, "AGENDADO");
        scheduler.agendarNotificacoesPreSessao();
        agendamento.setDataHora(agora.minusHours(4));
        agendamento.setStatus("CONCLUIDO");
        agendamentos.saveAndFlush(agendamento);
        scheduler.agendarNotificacoesPosSessao();
        assertThat(envios.findAll()).extracting(NotificacaoEnvio::getTipo)
                .containsExactlyInAnyOrder(TipoNotificacao.PRE_SESSAO, TipoNotificacao.POS_SESSAO);
    }

    @Test
    void chamadasConcorrentesNaoDevemDuplicarEnvio() throws Exception {
        Agendamento agendamento = salvar(agora.plusHours(1), 60, "AGENDADO");
        var executor = Executors.newFixedThreadPool(2);
        try {
            var primeira = executor.submit(() -> service.enviarNotificacaoCuidadosPreSessao(agendamento.getId()));
            var segunda = executor.submit(() -> service.enviarNotificacaoCuidadosPreSessao(agendamento.getId()));
            primeira.get(10, TimeUnit.SECONDS);
            segunda.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        verify(sender).enviarTemplate(anyString(), anyString(), anyString(), anyList(), anyString());
        assertThat(envios.count()).isEqualTo(1);
    }

    private Agendamento salvar(LocalDateTime inicio, int duracao, String status) {
        return agendamentos.saveAndFlush(Agendamento.builder().clienteId(cliente.getId()).tatuadorId(1L)
                .dataHora(inicio).duracaoMinutos(duracao).status(status).build());
    }
}
