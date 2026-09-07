package com.tattooflow.modules.financeiro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private FinanceiroRepository financeiroRepository;

    @InjectMocks
    private FinanceiroService financeiroService;

    @Test
    void deveListarPagamentosComSucesso() {
        Pagamento mockPagamento = Pagamento.builder().id(1L).valor(new BigDecimal("350.00")).status("CONCLUIDO").build();
        when(financeiroRepository.findAll()).thenReturn(List.of(mockPagamento));

        List<Pagamento> pagamentos = financeiroService.listarPagamentos();

        assertNotNull(pagamentos);
        assertEquals(1, pagamentos.size());
        verify(financeiroRepository, times(1)).findAll();
    }
}
