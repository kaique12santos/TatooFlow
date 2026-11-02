package com.tattooflow.modules.relatorio;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import com.tattooflow.modules.estoque.EstoqueRepository;
import com.tattooflow.modules.estoque.ItemEstoque;
import com.tattooflow.modules.financeiro.FinanceiroRepository;
import com.tattooflow.modules.financeiro.Pagamento;
import com.tattooflow.modules.financeiro.Repasse;
import com.tattooflow.modules.financeiro.RepasseRepository;
import com.tattooflow.modules.usuario.Usuario;
import com.tattooflow.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private final RepasseRepository repasseRepository;
    private final FinanceiroRepository financeiroRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final UsuarioRepository usuarioRepository;

    public RelatorioService(RepasseRepository repasseRepository,
                            FinanceiroRepository financeiroRepository,
                            AgendamentoRepository agendamentoRepository,
                            EstoqueRepository estoqueRepository,
                            UsuarioRepository usuarioRepository) {
        this.repasseRepository = repasseRepository;
        this.financeiroRepository = financeiroRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.estoqueRepository = estoqueRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioMensalDTO gerarRelatorioMensal(int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new BusinessException("Mês inválido. O mês deve estar entre 1 e 12.");
        }
        if (ano < 2000 || ano > 2100) {
            throw new BusinessException("Ano inválido. Informe um ano entre 2000 e 2100.");
        }

        YearMonth yearMonth = YearMonth.of(ano, mes);
        LocalDateTime inicio = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime fim = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 1. Buscar repasses no período
        List<Repasse> repasses = repasseRepository.buscarPorPeriodo(inicio, fim);

        // 2. Buscar agendamentos concluídos e totais no período
        List<Agendamento> agendamentosConcluidos = agendamentoRepository.listarPorPeriodoEStatus(inicio, fim, "CONCLUIDO", null);
        List<Agendamento> todosAgendamentosPeriodo = agendamentoRepository.listarPorPeriodoEStatus(inicio, fim, null, null);
        Map<Long, Agendamento> agendamentoMap = todosAgendamentosPeriodo.stream()
                .collect(Collectors.toMap(Agendamento::getId, a -> a, (existente, novo) -> existente));

        // 3. Buscar pagamentos no período
        List<Pagamento> pagamentos = financeiroRepository.buscarPorPeriodo(inicio, fim);
        BigDecimal faturamentoRecebido = pagamentos.stream()
                .filter(p -> "CONCLUIDO".equalsIgnoreCase(p.getStatus()))
                .map(p -> {
                    BigDecimal valor = p.getValor() != null ? p.getValor() : BigDecimal.ZERO;
                    return "REEMBOLSO".equalsIgnoreCase(p.getTipo()) ? valor.negate() : valor;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 4. Mapeamento de nomes de tatuadores
        Map<Long, String> nomesTatuadores = new HashMap<>();

        // 5. Processamento item a item dos repasses
        BigDecimal totalGastosInsumos = BigDecimal.ZERO;
        BigDecimal totalRepasseArtistas = BigDecimal.ZERO;
        BigDecimal totalComissoesEstudio = BigDecimal.ZERO;
        BigDecimal totalFaturamentoBruto = BigDecimal.ZERO;

        List<RelatorioRepasseItemDTO> detalhamentoRepasses = new ArrayList<>();
        Map<Long, List<RepasseCalculado>> repassesPorArtista = new LinkedHashMap<>();

        for (Repasse r : repasses) {
            Long tatuadorId = r.getTatuadorId();
            String nomeTatuador = nomesTatuadores.computeIfAbsent(tatuadorId, id ->
                    usuarioRepository.findById(id).map(Usuario::getNome).orElse("Tatuador #" + id));

            BigDecimal custoInsumos = r.getCustoInsumos() != null ? r.getCustoInsumos() : BigDecimal.ZERO;
            BigDecimal valorArtista = r.getValorRepasse() != null ? r.getValorRepasse() : BigDecimal.ZERO;

            Agendamento ag = r.getAgendamentoId() != null ? agendamentoMap.get(r.getAgendamentoId()) : null;
            BigDecimal valorTotalSessao;
            BigDecimal baseCalculo;
            BigDecimal comissaoEstudio;

            if (ag != null && ag.getValorEstimado() != null && ag.getValorEstimado().compareTo(BigDecimal.ZERO) > 0) {
                valorTotalSessao = ag.getValorEstimado().setScale(2, RoundingMode.HALF_UP);
                baseCalculo = valorTotalSessao.subtract(custoInsumos).setScale(2, RoundingMode.HALF_UP);
                comissaoEstudio = baseCalculo.subtract(valorArtista).setScale(2, RoundingMode.HALF_UP);
            } else {
                if (valorArtista.compareTo(BigDecimal.ZERO) > 0) {
                    comissaoEstudio = valorArtista.multiply(new BigDecimal("30"))
                            .divide(new BigDecimal("70"), 2, RoundingMode.HALF_UP);
                    baseCalculo = valorArtista.add(comissaoEstudio).setScale(2, RoundingMode.HALF_UP);
                    valorTotalSessao = baseCalculo.add(custoInsumos).setScale(2, RoundingMode.HALF_UP);
                } else {
                    comissaoEstudio = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    baseCalculo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    valorTotalSessao = custoInsumos.setScale(2, RoundingMode.HALF_UP);
                }
            }

            totalGastosInsumos = totalGastosInsumos.add(custoInsumos);
            totalRepasseArtistas = totalRepasseArtistas.add(valorArtista);
            totalComissoesEstudio = totalComissoesEstudio.add(comissaoEstudio);
            totalFaturamentoBruto = totalFaturamentoBruto.add(valorTotalSessao);

            detalhamentoRepasses.add(RelatorioRepasseItemDTO.builder()
                    .repasseId(r.getId())
                    .agendamentoId(r.getAgendamentoId())
                    .tatuadorId(tatuadorId)
                    .nomeTatuador(nomeTatuador)
                    .data(r.getDataRepasse())
                    .valorTotal(valorTotalSessao)
                    .custoInsumos(custoInsumos)
                    .baseCalculo(baseCalculo)
                    .comissaoEstudio(comissaoEstudio)
                    .valorRepasseArtista(valorArtista)
                    .status(r.getStatus())
                    .build());

            repassesPorArtista.computeIfAbsent(tatuadorId, k -> new ArrayList<>())
                    .add(new RepasseCalculado(r, valorTotalSessao, baseCalculo, comissaoEstudio, valorArtista, custoInsumos));
        }

        // Se não houver repasses cadastrados mas houver agendamentos concluídos:
        if (repasses.isEmpty() && !agendamentosConcluidos.isEmpty()) {
            for (Agendamento ag : agendamentosConcluidos) {
                BigDecimal valor = ag.getValorEstimado() != null ? ag.getValorEstimado() : BigDecimal.ZERO;
                totalFaturamentoBruto = totalFaturamentoBruto.add(valor);
            }
        }

        // Se totalFaturamentoBruto for 0 e faturamentoRecebido for maior que 0, reflete faturamentoRecebido
        if (totalFaturamentoBruto.compareTo(BigDecimal.ZERO) == 0 && faturamentoRecebido.compareTo(BigDecimal.ZERO) > 0) {
            totalFaturamentoBruto = faturamentoRecebido;
        }

        totalGastosInsumos = totalGastosInsumos.setScale(2, RoundingMode.HALF_UP);
        totalRepasseArtistas = totalRepasseArtistas.setScale(2, RoundingMode.HALF_UP);
        totalComissoesEstudio = totalComissoesEstudio.setScale(2, RoundingMode.HALF_UP);
        totalFaturamentoBruto = totalFaturamentoBruto.setScale(2, RoundingMode.HALF_UP);

        BigDecimal lucroLiquidoEstudio = totalComissoesEstudio;

        // 6. Agrupamento por Artista
        List<RelatorioArtistaDTO> detalhamentoArtistas = new ArrayList<>();
        for (Map.Entry<Long, List<RepasseCalculado>> entry : repassesPorArtista.entrySet()) {
            Long tatuadorId = entry.getKey();
            List<RepasseCalculado> lista = entry.getValue();
            String nome = nomesTatuadores.get(tatuadorId);

            BigDecimal faturadoArtista = lista.stream().map(RepasseCalculado::valorTotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            BigDecimal baseArtista = lista.stream().map(RepasseCalculado::baseCalculo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            BigDecimal comissaoArtista = lista.stream().map(RepasseCalculado::comissaoEstudio).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            BigDecimal repasseDevido = lista.stream().map(RepasseCalculado::valorArtista).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            BigDecimal insumosArtista = lista.stream().map(RepasseCalculado::custoInsumos).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

            detalhamentoArtistas.add(RelatorioArtistaDTO.builder()
                    .tatuadorId(tatuadorId)
                    .nomeTatuador(nome)
                    .totalSessoes(lista.size())
                    .totalFaturado(faturadoArtista)
                    .baseCalculo(baseArtista)
                    .comissaoEstudio(comissaoArtista)
                    .repasseArtista(repasseDevido)
                    .custoInsumos(insumosArtista)
                    .build());
        }

        // 7. Resumo do Estoque (F7-BANCO-01)
        long totalItensEstoque = estoqueRepository.count();
        List<ItemEstoque> criticos = estoqueRepository.buscarItensEmEstoqueMinimo();
        long itensCriticos = criticos != null ? criticos.size() : 0;

        RelatorioEstoqueResumoDTO resumoEstoque = RelatorioEstoqueResumoDTO.builder()
                .totalItensCadastrados(totalItensEstoque)
                .itensEmEstoqueCritico(itensCriticos)
                .alertaEstoqueAtivo(itensCriticos > 0)
                .build();

        int totalSessoes = repasses.size() > 0 ? repasses.size() : agendamentosConcluidos.size();

        return RelatorioMensalDTO.builder()
                .mes(mes)
                .ano(ano)
                .periodo(String.format("%02d/%04d", mes, ano))
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalFaturamento(totalFaturamentoBruto)
                .faturamentoBruto(totalFaturamentoBruto)
                .faturamentoRecebido(faturamentoRecebido)
                .totalComissoes(totalComissoesEstudio)
                .comissaoEstudio(totalComissoesEstudio)
                .totalRepasses(totalRepasseArtistas)
                .totalRepasseArtistas(totalRepasseArtistas)
                .totalGastos(totalGastosInsumos)
                .totalGastosInsumos(totalGastosInsumos)
                .lucroLiquido(lucroLiquidoEstudio)
                .lucroLiquidoEstudio(lucroLiquidoEstudio)
                .totalSessoes(totalSessoes)
                .totalSessoesConcluidas(agendamentosConcluidos.size())
                .totalAgendamentosPeriodo(todosAgendamentosPeriodo.size())
                .detalhamentoArtistas(detalhamentoArtistas)
                .detalhamentoRepasses(detalhamentoRepasses)
                .resumoEstoque(resumoEstoque)
                .build();
    }

    private record RepasseCalculado(Repasse repasse, BigDecimal valorTotal, BigDecimal baseCalculo,
                                   BigDecimal comissaoEstudio, BigDecimal valorArtista, BigDecimal custoInsumos) {}
}
