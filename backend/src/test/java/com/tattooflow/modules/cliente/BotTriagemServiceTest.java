package com.tattooflow.modules.cliente;

import com.tattooflow.integrations.whatsapp.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotTriagemServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PreAgendamentoRepository preAgendamentoRepository;

    @Mock
    private WhatsAppMessageSender messageSender;

    private BotTriagemService service;

    private Cliente clienteTeste;

    @BeforeEach
    void setUp() {
        service = new BotTriagemService(clienteRepository, preAgendamentoRepository, messageSender);

        clienteTeste = Cliente.builder()
                .id(1L)
                .nome("Fernanda Costa")
                .telefone("5511999991234")
                .build();

        lenient().when(clienteRepository.findByTelefone("5511999991234"))
                .thenReturn(Optional.of(clienteTeste));

        lenient().when(preAgendamentoRepository.save(any(PreAgendamento.class)))
                .thenAnswer(invocation -> {
                    PreAgendamento p = invocation.getArgument(0);
                    if (p.getId() == null) {
                        p.setId(10L);
                    }
                    return p;
                });
    }

    @Test
    void deveIniciarTriagemComPerguntaDeArteParaNovaMensagem() {
        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.empty());
        when(preAgendamentoRepository.findFirstByClienteOrderByDataCriacaoDesc(clienteTeste))
                .thenReturn(Optional.empty());

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "Olá, boa tarde!");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_ARTE);
        assertThat(resposta.textoEnviado()).contains("ideia ou desenho");

        verify(messageSender).enviarMensagem(eq("5511999991234"), contains("ideia ou desenho"));
    }

    @Test
    void deveRejeitarArteInvalidaOuMuitoCurtaComMensagemOrientativa() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_ARTE)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.of(pre));

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "12");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_ARTE);
        assertThat(resposta.textoEnviado()).contains("Não consegui entender");
        assertThat(pre.getIdeiasDescricao()).isNull();
    }

    @Test
    void deveAvancarParaLocalAposArteValida() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_ARTE)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.of(pre));

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "Quero tatuar um leão com flor de lótus");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_LOCAL);
        assertThat(resposta.textoEnviado()).contains("local do corpo");
        assertThat(pre.getIdeiasDescricao()).isEqualTo("Quero tatuar um leão com flor de lótus");
        assertThat(pre.getEtapaBot()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_LOCAL);
    }

    @Test
    void deveRejeitarLocalInvalidoComMensagemOrientativa() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .ideiasDescricao("Leão com lótus")
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_LOCAL)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.of(pre));

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", ".");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_LOCAL);
        assertThat(resposta.textoEnviado()).contains("indique uma região do corpo válida");
        assertThat(pre.getLocalCorpo()).isNull();
    }

    @Test
    void deveAvancarParaDataAposLocalValido() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .ideiasDescricao("Leão com lótus")
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_LOCAL)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.of(pre));

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "Antebraço direito");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_DATA);
        assertThat(resposta.textoEnviado()).contains("quando você gostaria de agendar");
        assertThat(pre.getLocalCorpo()).isEqualTo("Antebraço direito");
        assertThat(pre.getEtapaBot()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_DATA);
    }

    @Test
    void deveFinalizarPreAgendamentoComSucessoEExibirResumo() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .ideiasDescricao("Leão com lótus")
                .localCorpo("Antebraço direito")
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_DATA)
                .dataCriacao(LocalDateTime.now())
                .build();

        when(preAgendamentoRepository.findFirstByClienteAndStatusTriagemOrderByDataCriacaoDesc(clienteTeste, "EM_TRIAGEM"))
                .thenReturn(Optional.of(pre));

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "Próximo sábado à tarde");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_FINALIZADO);
        assertThat(pre.getStatusTriagem()).isEqualTo(BotTriagemService.STATUS_PENDENTE);
        assertThat(pre.getDataDesejada()).isEqualTo("Próximo sábado à tarde");

        assertThat(resposta.textoEnviado())
                .contains("concluído com sucesso")
                .contains("Leão com lótus")
                .contains("Antebraço direito")
                .contains("Próximo sábado à tarde");
    }

    @Test
    void deveReiniciarAtendimentoAoReceberComandoReiniciar() {
        PreAgendamento pre = PreAgendamento.builder()
                .id(10L)
                .cliente(clienteTeste)
                .ideiasDescricao("Ideia antiga")
                .statusTriagem("EM_TRIAGEM")
                .etapaBot(BotTriagemService.ETAPA_AGUARDANDO_LOCAL)
                .dataCriacao(LocalDateTime.now())
                .build();

        BotTriagemService.RespostaBot resposta = service.processarMensagem(
                "5511999991234", "Fernanda Costa", "reiniciar");

        assertThat(resposta.etapaAtual()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_ARTE);
        assertThat(resposta.textoEnviado()).contains("Reiniciamos seu atendimento");

        ArgumentCaptor<PreAgendamento> captor = ArgumentCaptor.forClass(PreAgendamento.class);
        verify(preAgendamentoRepository, atLeastOnce()).save(captor.capture());

        PreAgendamento salvo = captor.getValue();
        assertThat(salvo.getEtapaBot()).isEqualTo(BotTriagemService.ETAPA_AGUARDANDO_ARTE);
        assertThat(salvo.getStatusTriagem()).isEqualTo(BotTriagemService.STATUS_EM_TRIAGEM);
    }
}
