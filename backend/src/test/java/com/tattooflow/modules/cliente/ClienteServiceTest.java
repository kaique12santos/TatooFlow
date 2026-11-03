package com.tattooflow.modules.cliente;

import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.midia.Midia;
import com.tattooflow.modules.midia.MidiaRepository;
import com.tattooflow.modules.termo.TermoAceite;
import com.tattooflow.modules.termo.TermoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PreAgendamentoRepository preAgendamentoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private TermoRepository termoRepository;

    @Mock
    private MidiaRepository midiaRepository;

    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteService(
                clienteRepository,
                preAgendamentoRepository,
                agendamentoRepository,
                termoRepository,
                midiaRepository
        );
    }

    @Test
    @DisplayName("buscarClientes deve buscar por termo e consolidar tipos de trabalho, orçamentos e sessões")
    void deveBuscarClientesComSucesso() {
        Cliente c = Cliente.builder().id(1L).nome("Carlos").email("carlos@teste.com").build();
        when(clienteRepository.buscarPorNomeOuTipoTrabalho("Dragão")).thenReturn(List.of(c));

        PreAgendamento pa = PreAgendamento.builder()
                .id(10L)
                .ideiasDescricao("Dragão Oriental costas")
                .referencia("ref-link")
                .statusTriagem("PENDENTE")
                .dataCriacao(LocalDateTime.now())
                .build();
        when(preAgendamentoRepository.findByClienteOrderByDataCriacaoDesc(c)).thenReturn(List.of(pa));

        Agendamento ag = Agendamento.builder()
                .id(20L)
                .clienteId(1L)
                .tatuadorId(2L)
                .descricaoSessao("Sessão Dragão 1")
                .dataHora(LocalDateTime.now())
                .duracaoMinutos(180)
                .valorEstimado(new BigDecimal("900.00"))
                .status("CONFIRMADO")
                .build();
        when(agendamentoRepository.findByClienteIdOrderByDataHoraDesc(1L)).thenReturn(List.of(ag));

        List<ClienteBuscaResponse> result = clienteService.buscarClientes("Dragão");

        assertThat(result).hasSize(1);
        ClienteBuscaResponse resp = result.get(0);
        assertThat(resp.getNome()).isEqualTo("Carlos");
        assertThat(resp.getTipoTrabalho()).isEqualTo("Sessão Dragão 1");
        assertThat(resp.getTiposTrabalho()).contains("Sessão Dragão 1", "Dragão Oriental costas", "ref-link");
        assertThat(resp.getOrcamentos()).hasSize(1);
        assertThat(resp.getSessoes()).hasSize(1);
    }

    @Test
    @DisplayName("buscarClientes com termo nulo ou em branco deve retornar todos ordenados por nome")
    void deveBuscarTodosQuandoTermoVazio() {
        Cliente c = Cliente.builder().id(1L).nome("Ana").build();
        when(clienteRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(c));

        List<ClienteBuscaResponse> result = clienteService.buscarClientes("   ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Ana");
        verify(clienteRepository, times(1)).findAllByOrderByNomeAsc();
        verify(clienteRepository, never()).buscarPorNomeOuTipoTrabalho(any());
    }

    @Test
    @DisplayName("buscarHistoricoCompleto deve retornar sessões com mídias, termos e totais")
    void deveBuscarHistoricoCompleto() {
        Cliente c = Cliente.builder().id(1L).nome("Beatriz").telefone("11999991111").build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(c));

        Agendamento ag = Agendamento.builder()
                .id(50L)
                .clienteId(1L)
                .tatuadorId(3L)
                .dataHora(LocalDateTime.now())
                .duracaoMinutos(60)
                .descricaoSessao("Aquarela Floral")
                .status("CONCLUIDO")
                .build();
        when(agendamentoRepository.findByClienteIdOrderByDataHoraDesc(1L)).thenReturn(List.of(ag));

        TermoAceite termo = TermoAceite.builder()
                .id(100L)
                .clienteId(1L)
                .agendamentoId(50L)
                .urlPdfAssinado("https://pdf.com/termo1.pdf")
                .dataAssinatura(LocalDateTime.now())
                .build();
        when(termoRepository.findByClienteIdOrderByDataAssinaturaDesc(1L)).thenReturn(List.of(termo));

        Midia midia = Midia.builder()
                .id(200L)
                .agendamentoId(50L)
                .url("https://img.com/foto1.jpg")
                .tipo("FOTO_DEPOIS")
                .build();
        when(midiaRepository.findByAgendamentoIdInOrderByDataUploadDesc(List.of(50L))).thenReturn(List.of(midia));

        ClienteHistoricoResponse hist = clienteService.buscarHistoricoCompleto(1L);

        assertThat(hist.getCliente().getNome()).isEqualTo("Beatriz");
        assertThat(hist.getTotalSessoes()).isEqualTo(1);
        assertThat(hist.getTotalTermos()).isEqualTo(1);
        assertThat(hist.getTotalMidias()).isEqualTo(1);
        assertThat(hist.getSessoes().get(0).getMidias()).hasSize(1);
        assertThat(hist.getSessoes().get(0).getMidias().get(0).getUrl()).isEqualTo("https://img.com/foto1.jpg");
        assertThat(hist.getMidias()).hasSize(1);
        assertThat(hist.getTermos()).hasSize(1);
    }

    @Test
    @DisplayName("buscarHistoricoCompleto deve lançar ResourceNotFoundException quando cliente não existir")
    void deveLancarExcecaoQuandoClienteNaoEncontradoNoHistorico() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarHistoricoCompleto(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado com ID: 99");
    }
}

