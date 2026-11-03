package com.tattooflow.modules.postagem;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.midia.MidiaRepository;
import com.tattooflow.service.InstagramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PostagemServiceTest {

    private PostagemAgendadaRepository postagemAgendadaRepository;
    private PostagemLogRepository postagemLogRepository;
    private InstagramService instagramService;
    private MidiaRepository midiaRepository;
    private PostagemService postagemService;

    @BeforeEach
    void setUp() {
        postagemAgendadaRepository = mock(PostagemAgendadaRepository.class);
        postagemLogRepository = mock(PostagemLogRepository.class);
        instagramService = mock(InstagramService.class);
        midiaRepository = mock(MidiaRepository.class);

        postagemService = new PostagemService(
                postagemAgendadaRepository,
                postagemLogRepository,
                instagramService,
                midiaRepository
        );
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve agendar postagem futura com status AGENDADO e registrar log de auditoria")
    void deveAgendarPostagemFuturaComSucesso() {
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(2);
        AgendarPostagemRequest request = AgendarPostagemRequest.builder()
                .imageUrl("https://res.cloudinary.com/demo/image/upload/v1/obra.webp")
                .legenda("Legenda com #TattooFlow e RN03")
                .dataHoraAgendada(dataFutura)
                .build();

        when(postagemAgendadaRepository.save(any(PostagemAgendada.class))).thenAnswer(invocation -> {
            PostagemAgendada p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        PostagemAgendadaResponse response = postagemService.agendarPostagem(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getProtocolo()).startsWith("TATTOO-MKT-");
        assertThat(response.getStatus()).isEqualTo("AGENDADO");

        // Verifica que o log de auditoria foi persistido
        verify(postagemLogRepository).save(argThat(log ->
                log.getPostagemAgendadaId().equals(10L) &&
                "AGENDAMENTO_CRIADO".equals(log.getTipoEvento()) &&
                log.isSucesso()
        ));

        // Como a postagem é futura, a Graph API NÃO deve ser chamada imediatamente
        verify(instagramService, never()).publicarFoto(any(), any());
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve disparar e publicar postagem com sucesso via executarPublicacao")
    void deveExecutarPublicacaoComSucesso() {
        PostagemAgendada postagem = PostagemAgendada.builder()
                .id(20L)
                .imageUrl("https://res.cloudinary.com/demo/image/upload/v1/obra.webp")
                .legenda("Legenda imediata")
                .protocolo("TATTOO-MKT-IMEDIATO-01")
                .status("AGENDADO")
                .tentativas(0)
                .build();

        when(postagemAgendadaRepository.save(any(PostagemAgendada.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublicacaoResultado resultadoSucesso = PublicacaoResultado.sucesso("container_1", "media_1", "{\"id\":\"media_1\"}");
        when(instagramService.publicarFoto(any(), any())).thenReturn(resultadoSucesso);

        PostagemAgendada resultado = postagemService.executarPublicacao(postagem);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo("PUBLICADO");
        assertThat(resultado.getInstagramMediaId()).isEqualTo("media_1");
        assertThat(resultado.getInstagramContainerId()).isEqualTo("container_1");
        verify(instagramService, times(1)).publicarFoto(eq(postagem.getImageUrl()), eq(postagem.getLegenda()));

        // Verifica logs: DISPARO_INICIADO e PUBLICADO_SUCESSO
        verify(postagemLogRepository, times(2)).save(any(PostagemLog.class));
    }

    @Test
    @DisplayName("[F4-BACK-03] Job deve processar postagens pendentes e atualizar para PUBLICADO")
    void deveProcessarPostagensPendentesNoJobComSucesso() {
        PostagemAgendada pendente = PostagemAgendada.builder()
                .id(1L)
                .imageUrl("https://imagem.jpg")
                .legenda("Legenda pendente")
                .status("AGENDADO")
                .dataHoraAgendada(LocalDateTime.now().minusMinutes(5))
                .protocolo("TATTOO-MKT-20250523-111111")
                .tentativas(0)
                .build();

        when(postagemAgendadaRepository.findByStatusAndDataHoraAgendadaLessThanEqual(eq("AGENDADO"), any(LocalDateTime.class)))
                .thenReturn(List.of(pendente));
        when(postagemAgendadaRepository.save(any(PostagemAgendada.class))).thenAnswer(i -> i.getArgument(0));

        when(instagramService.publicarFoto(any(), any()))
                .thenReturn(PublicacaoResultado.sucesso("container_abc", "media_xyz", "{\"id\":\"media_xyz\"}"));

        int processadas = postagemService.processarPostagensPendentes();

        assertThat(processadas).isEqualTo(1);
        assertThat(pendente.getStatus()).isEqualTo("PUBLICADO");
        assertThat(pendente.getInstagramMediaId()).isEqualTo("media_xyz");
        assertThat(pendente.getInstagramContainerId()).isEqualTo("container_abc");
        assertThat(pendente.getDataHoraPublicacao()).isNotNull();

        verify(postagemLogRepository).save(argThat(log ->
                "PUBLICADO_SUCESSO".equals(log.getTipoEvento()) && log.isSucesso()
        ));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve registrar status ERRO e log de falha quando Instagram Graph API rejeitar")
    void deveRegistrarErroELogDeFalhaQuandoInstagramFalhar() {
        PostagemAgendada pendente = PostagemAgendada.builder()
                .id(2L)
                .imageUrl("https://imagem-invalida.jpg")
                .legenda("Legenda com erro")
                .status("AGENDADO")
                .dataHoraAgendada(LocalDateTime.now().minusMinutes(2))
                .protocolo("TATTOO-MKT-20250523-222222")
                .tentativas(0)
                .build();

        when(postagemAgendadaRepository.findByStatusAndDataHoraAgendadaLessThanEqual(eq("AGENDADO"), any(LocalDateTime.class)))
                .thenReturn(List.of(pendente));
        when(postagemAgendadaRepository.save(any(PostagemAgendada.class))).thenAnswer(i -> i.getArgument(0));

        when(instagramService.publicarFoto(any(), any()))
                .thenReturn(PublicacaoResultado.falha("Erro HTTP 400 da Graph API", "{\"error\":{\"code\":100}}"));

        int processadas = postagemService.processarPostagensPendentes();

        assertThat(processadas).isEqualTo(1);
        assertThat(pendente.getStatus()).isEqualTo("ERRO");
        assertThat(pendente.getTentativas()).isEqualTo(1);
        assertThat(pendente.getMensagemErro()).contains("Erro HTTP 400");

        verify(postagemLogRepository).save(argThat(log ->
                "FALHA_PUBLICACAO".equals(log.getTipoEvento()) && !log.isSucesso()
        ));
    }

    @Test
    @DisplayName("[F4-BACK-03] Deve rejeitar agendamento sem URL de imagem ou sem legenda")
    void deveRejeitarAgendamentoIncompleto() {
        assertThatThrownBy(() -> postagemService.agendarPostagem(AgendarPostagemRequest.builder().legenda("Texto").build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("URL da imagem é obrigatória");

        assertThatThrownBy(() -> postagemService.agendarPostagem(AgendarPostagemRequest.builder().imageUrl("https://imagem.jpg").build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("legenda é obrigatória");
    }
}

