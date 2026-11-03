package com.tattooflow.modules.termo;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermoServiceTest {

    @Mock
    private TermoRepository termoRepository;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private TermoService termoService;

    private LocalDateTime dataAssinatura;

    @BeforeEach
    void setUp() {
        dataAssinatura = LocalDateTime.of(2026, 10, 27, 10, 0, 0);
    }

    @Test
    @DisplayName("RN06: Deve preencher automaticamente a expiração da retenção com 5 anos a partir da data de assinatura")
    void devePreencherDataExpiracaoRetencaoAutomaticamenteComMinimo5Anos() {
        TermoAceite termo = TermoAceite.builder()
                .clienteId(1L)
                .agendamentoId(10L)
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-1.pdf")
                .ipAssinatura("192.168.1.100")
                .dataAssinatura(dataAssinatura)
                .build();

        when(termoRepository.save(any(TermoAceite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TermoAceite salvo = termoService.salvar(termo);

        assertThat(salvo.getDataExpiracaoRetencao()).isNotNull();
        assertThat(salvo.getDataExpiracaoRetencao()).isEqualTo(dataAssinatura.plusYears(5));
        verify(termoRepository, times(1)).save(termo);
    }

    @Test
    @DisplayName("RN06: Deve lançar BusinessException quando a retenção informada for menor do que 5 anos")
    void deveLancarExcecaoQuandoRetencaoMenorQue5Anos() {
        TermoAceite termo = TermoAceite.builder()
                .clienteId(1L)
                .agendamentoId(10L)
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-1.pdf")
                .ipAssinatura("192.168.1.100")
                .dataAssinatura(dataAssinatura)
                .dataExpiracaoRetencao(dataAssinatura.plusYears(4)) // Menor que 5 anos
                .build();

        assertThatThrownBy(() -> termoService.salvar(termo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RN06: O prazo de expiração da retenção deve ser de no mínimo 5 anos");

        verify(termoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN06: Deve aceitar retenção informada igual ou maior que 5 anos")
    void deveAceitarRetencaoMaiorOuIgualA5Anos() {
        LocalDateTime expiracao7Anos = dataAssinatura.plusYears(7);
        TermoAceite termo = TermoAceite.builder()
                .clienteId(1L)
                .agendamentoId(10L)
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-1.pdf")
                .ipAssinatura("192.168.1.100")
                .dataAssinatura(dataAssinatura)
                .dataExpiracaoRetencao(expiracao7Anos)
                .build();

        when(termoRepository.save(any(TermoAceite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TermoAceite salvo = termoService.salvar(termo);

        assertThat(salvo.getDataExpiracaoRetencao()).isEqualTo(expiracao7Anos);
        verify(termoRepository, times(1)).save(termo);
    }

    @Test
    @DisplayName("Deve validar obrigatoriedade do clienteId")
    void deveValidarClienteIdObrigatorio() {
        TermoAceite termo = TermoAceite.builder()
                .agendamentoId(10L)
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-1.pdf")
                .build();

        assertThatThrownBy(() -> termoService.salvar(termo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clienteId é obrigatório");
    }

    @Test
    @DisplayName("Deve validar obrigatoriedade do agendamentoId")
    void deveValidarAgendamentoIdObrigatorio() {
        TermoAceite termo = TermoAceite.builder()
                .clienteId(1L)
                .urlPdfAssinado("https://storage.exemplo.com/termos/termo-1.pdf")
                .build();

        assertThatThrownBy(() -> termoService.salvar(termo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("agendamentoId é obrigatório");
    }

    @Test
    @DisplayName("Deve validar obrigatoriedade da urlPdfAssinado")
    void deveValidarUrlPdfAssinadoObrigatorio() {
        TermoAceite termo = TermoAceite.builder()
                .clienteId(1L)
                .agendamentoId(10L)
                .urlPdfAssinado("  ")
                .build();

        assertThatThrownBy(() -> termoService.salvar(termo))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("urlPdfAssinado é obrigatório");
    }

    @Test
    @DisplayName("Deve buscar termo por id com sucesso")
    void deveBuscarPorIdComSucesso() {
        TermoAceite termo = TermoAceite.builder().id(1L).clienteId(1L).agendamentoId(10L).urlPdfAssinado("url").build();
        when(termoRepository.findById(1L)).thenReturn(Optional.of(termo));

        TermoAceite resultado = termoService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar termo inexistente")
    void deveLancarExceptionQuandoTermoNaoEncontrado() {
        when(termoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> termoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Termo não encontrado com ID: 99");
    }

    @Test
    @DisplayName("Deve listar todos os termos cadastrados")
    void deveListarTodos() {
        TermoAceite t1 = TermoAceite.builder().id(1L).build();
        TermoAceite t2 = TermoAceite.builder().id(2L).build();
        when(termoRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TermoAceite> resultado = termoService.listarTodos();

        assertThat(resultado).hasSize(2);
    }
}
