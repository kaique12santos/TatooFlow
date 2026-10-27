package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sinal_persistence_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
}, showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
@Import(SinalService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class SinalPersistenceTest {
    @Autowired
    private SinalService service;
    @Autowired
    private FinanceiroRepository pagamentos;
    @SpyBean
    private AgendamentoRepository agendamentos;
    private Long id;

    @BeforeEach
    void preparar() {
        pagamentos.deleteAll();
        agendamentos.deleteAll();
        id = agendamentos.save(Agendamento.builder().clienteId(1L).tatuadorId(1L)
                .dataHora(LocalDateTime.of(2026, 11, 10, 14, 0)).duracaoMinutos(120)
                .valorEstimado(new BigDecimal("350.00")).status("AGENDADO").build()).getId();
    }

    private MockMultipartFile comprovante() {
        return new MockMultipartFile("comprovante", "sinal.pdf", "application/pdf",
                "%PDF-1.4\ncomprovante".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void devePersistirPagamentoArquivoEConfirmacaoJuntos() throws Exception {
        service.registrar(id, new BigDecimal("175.00"), "PIX", comprovante());
        assertEquals("CONFIRMADO", agendamentos.findById(id).orElseThrow().getStatus());
        assertEquals(1, pagamentos.count());
        assertArrayEquals(comprovante().getBytes(), service.buscarComprovante(id).conteudo());
    }

    @Test
    void deveReverterPagamentoEArquivoQuandoConfirmacaoFalhar() {
        doThrow(new IllegalStateException("Falha ao confirmar")).when(agendamentos).save(any(Agendamento.class));
        assertThrows(IllegalStateException.class,
                () -> service.registrar(id, new BigDecimal("175.00"), "PIX", comprovante()));
        assertEquals(0, pagamentos.count());
        assertEquals("AGENDADO", agendamentos.findById(id).orElseThrow().getStatus());
    }

    @Test
    void deveRegistrarApenasUmSinalEmRequisicoesConcorrentes() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(2);
        Callable<Boolean> registrar = () -> {
            inicio.countDown();
            if (!inicio.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("As requisições não iniciaram");
            }
            try {
                service.registrar(id, new BigDecimal("175.00"), "PIX", comprovante());
                return true;
            } catch (BusinessException ex) {
                assertTrue(ex.getMessage().contains("já foi registrado"));
                return false;
            }
        };
        try {
            Future<Boolean> primeira = executor.submit(registrar);
            Future<Boolean> segunda = executor.submit(registrar);
            List<Boolean> resultados = List.of(primeira.get(10, TimeUnit.SECONDS), segunda.get(10, TimeUnit.SECONDS));
            assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count());
            assertEquals(1, pagamentos.count());
            assertEquals("CONFIRMADO", agendamentos.findById(id).orElseThrow().getStatus());
        } finally {
            executor.shutdownNow();
        }
    }
}
