package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinalServiceTest {
    private static final byte[] PDF = "%PDF-1.4\ncomprovante".getBytes(StandardCharsets.UTF_8);
    @Mock
    private FinanceiroRepository pagamentos;
    @Mock
    private AgendamentoRepository agendamentos;
    @InjectMocks
    private SinalService service;
    private Agendamento agendamento;

    @BeforeEach
    void preparar() {
        agendamento = Agendamento.builder().id(1L).status("AGENDADO")
                .valorEstimado(new BigDecimal("350.00")).build();
    }

    private void encontrarAgendamento() {
        when(agendamentos.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(agendamento));
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("comprovante", "sinal.pdf", "application/pdf", PDF);
    }

    private void verificarSemRegistro() {
        verify(pagamentos, never()).save(any());
        verify(agendamentos, never()).save(any());
        assertEquals("AGENDADO", agendamento.getStatus());
    }

    @ParameterizedTest
    @CsvSource({"350.00,175.00", "100.01,50.01", "100.02,50.01"})
    void deveRegistrarMetadeDoValorComComprovanteAntesDeConfirmar(String total, String metade) {
        encontrarAgendamento();
        agendamento.setValorEstimado(new BigDecimal(total));
        when(pagamentos.save(any())).thenAnswer(invocation -> {
            assertEquals("AGENDADO", agendamento.getStatus());
            return invocation.getArgument(0);
        });

        Pagamento sinal = service.registrar(1L, new BigDecimal(metade), "PIX", pdf());

        assertAll(
                () -> assertEquals("CONFIRMADO", agendamento.getStatus()),
                () -> assertEquals(1L, sinal.getAgendamentoId()),
                () -> assertEquals(new BigDecimal(metade), sinal.getValor()),
                () -> assertEquals("SINAL", sinal.getTipo()),
                () -> assertEquals("CONCLUIDO", sinal.getStatus()),
                () -> assertNotNull(sinal.getDataPagamento()),
                () -> assertEquals("sinal.pdf", sinal.getComprovanteNome()),
                () -> assertEquals("application/pdf", sinal.getComprovanteContentType()),
                () -> assertArrayEquals(PDF, sinal.getComprovante()));
        var ordem = inOrder(pagamentos, agendamentos);
        ordem.verify(pagamentos).save(sinal);
        ordem.verify(agendamentos).save(agendamento);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-175", "174.99", "175.001", "175.01", "350"})
    void deveRecusarValorDiferenteDe50PorCento(String valor) {
        encontrarAgendamento();
        assertThrows(BusinessException.class, () -> service.registrar(1L,
                valor == null ? null : new BigDecimal(valor), "PIX", pdf()));
        verificarSemRegistro();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-1", "100.001"})
    void deveExigirOrcamentoValido(String total) {
        encontrarAgendamento();
        agendamento.setValorEstimado(total == null ? null : new BigDecimal(total));
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", pdf()));
        verificarSemRegistro();
    }

    @Test
    void deveRecusarSinalDuplicado() {
        encontrarAgendamento();
        when(pagamentos.existsByAgendamentoIdAndTipo(1L, "SINAL")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", pdf()));
        verificarSemRegistro();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"CANCELADO", "CONCLUIDO", "EM_ANDAMENTO", "CONFIRMADO"})
    void deveRecusarEstadoIncompativel(String status) {
        encontrarAgendamento();
        agendamento.setStatus(status);
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", pdf()));
        verify(pagamentos, never()).save(any());
        verify(agendamentos, never()).save(any());
        assertEquals(status, agendamento.getStatus());
    }

    @Test
    void deveRecusarAgendamentoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", pdf()));
        verificarSemRegistro();
    }

    @Test
    void deveExigirComprovanteNaoVazio() {
        encontrarAgendamento();
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", null));
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX",
                new MockMultipartFile("comprovante", new byte[0])));
        verificarSemRegistro();
    }

    @Test
    void deveRecusarArquivoAcimaDe5MB() {
        encontrarAgendamento();
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX",
                new MockMultipartFile("comprovante", new byte[5 * 1024 * 1024 + 1])));
        verificarSemRegistro();
    }

    @Test
    void deveRecusarConteudoOuTipoDeArquivoIncompativel() {
        encontrarAgendamento();
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX",
                new MockMultipartFile("comprovante", "falso.pdf", "application/pdf", new byte[]{1, 2, 3})));
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX",
                new MockMultipartFile("comprovante", "falso.png", "image/png", PDF)));
        verificarSemRegistro();
    }

    @Test
    void deveRecusarFalhaDeLeituraSemConfirmar() throws IOException {
        encontrarAgendamento();
        MultipartFile arquivo = mock(MultipartFile.class);
        when(arquivo.getBytes()).thenThrow(new IOException("Falha de leitura"));
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", arquivo));
        verificarSemRegistro();
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/jpeg", "image/jpg"})
    void deveAceitarComprovanteEmImagem(String tipo) {
        encontrarAgendamento();
        byte[] conteudo = "image/png".equals(tipo)
                ? new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10}
                : new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff};
        when(pagamentos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Pagamento sinal = service.registrar(1L, new BigDecimal("175.00"), "PIX",
                new MockMultipartFile("comprovante", "imagem", tipo, conteudo));
        String esperado = "image/png".equals(tipo) ? "image/png" : "image/jpeg";
        assertEquals(esperado, sinal.getComprovanteContentType());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"BOLETO", "", "pix"})
    void deveRecusarFormaDePagamentoInvalida(String forma) {
        encontrarAgendamento();
        assertThrows(BusinessException.class, () -> service.registrar(1L, new BigDecimal("175.00"), forma, pdf()));
        verificarSemRegistro();
    }

    @Test
    void naoDeveConfirmarSePagamentoFalharAoSerSalvo() {
        encontrarAgendamento();
        when(pagamentos.save(any())).thenThrow(new IllegalStateException("Falha ao salvar"));
        assertThrows(IllegalStateException.class, () -> service.registrar(1L, new BigDecimal("175.00"), "PIX", pdf()));
        assertEquals("AGENDADO", agendamento.getStatus());
        verify(agendamentos, never()).save(any());
    }
}
