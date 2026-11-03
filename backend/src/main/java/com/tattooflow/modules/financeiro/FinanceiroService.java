package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;
    private final RepasseRepository repasseRepository;
    private final AgendamentoRepository agendamentoRepository;

    public FinanceiroService(FinanceiroRepository financeiroRepository,
                             RepasseRepository repasseRepository,
                             AgendamentoRepository agendamentoRepository) {
        this.financeiroRepository = financeiroRepository;
        this.repasseRepository = repasseRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<Pagamento> listarPagamentos() {
        return financeiroRepository.findAll();
    }

    public Pagamento registrarPagamento(Pagamento pagamento) {
        if (pagamento.getId() != null) {
            throw new BusinessException("O registro não permite sobrescrever um pagamento existente");
        }
        if (pagamento.getTipo() != null && !"PAGAMENTO".equals(pagamento.getTipo())) {
            throw new BusinessException("Utilize o endpoint de sinal para registrar o sinal com comprovante");
        }
        pagamento.setTipo("PAGAMENTO");
        pagamento.setComprovanteNome(null);
        pagamento.setComprovanteContentType(null);
        pagamento.setComprovante(null);
        return financeiroRepository.save(pagamento);
    }

    /**
     * Regra RN02 e RN07: Divisão automática 30% estúdio / 70% artista,
     * com dedução do custo de insumos ANTES da divisão.
     */
    public DivisaoSessaoDTO calcularDivisao(BigDecimal valorTotal, BigDecimal custoInsumos) {
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor total da sessão deve ser positivo");
        }
        if (valorTotal.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("O valor total deve ter no máximo duas casas decimais");
        }
        BigDecimal insumos = custoInsumos != null ? custoInsumos : BigDecimal.ZERO;
        if (insumos.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("O custo de insumos não pode ser negativo");
        }
        if (insumos.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("O custo de insumos deve ter no máximo duas casas decimais");
        }
        if (insumos.compareTo(valorTotal) > 0) {
            throw new BusinessException("O custo de insumos não pode ser maior que o valor total da sessão");
        }

        BigDecimal baseCalculo = valorTotal.subtract(insumos).setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentagemEstudio = new BigDecimal("30.00");
        BigDecimal porcentagemArtista = new BigDecimal("70.00");

        BigDecimal valorEstudio = baseCalculo.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorArtista = baseCalculo.subtract(valorEstudio); // Garante que a soma bate exatamente com a base

        return DivisaoSessaoDTO.builder()
                .valorTotal(valorTotal)
                .custoInsumos(insumos)
                .baseCalculo(baseCalculo)
                .porcentagemEstudio(porcentagemEstudio)
                .valorEstudio(valorEstudio)
                .porcentagemArtista(porcentagemArtista)
                .valorArtista(valorArtista)
                .regra("RN02 (divisão 30/70) com dedução prévia de insumos (RN07)")
                .build();
    }

    public DivisaoSessaoDTO simularDivisaoAgendamento(Long agendamentoId, BigDecimal custoInsumos, BigDecimal valorTotalCustom) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));

        BigDecimal total = (valorTotalCustom != null && valorTotalCustom.compareTo(BigDecimal.ZERO) > 0)
                ? valorTotalCustom
                : agendamento.getValorEstimado();

        if (total == null) {
            throw new BusinessException("O agendamento não possui valor estimado e nenhum valor total foi informado");
        }

        DivisaoSessaoDTO divisao = calcularDivisao(total, custoInsumos);
        divisao.setAgendamentoId(agendamentoId);
        return divisao;
    }

    /**
     * Finaliza a sessão do agendamento, aplica a divisão automática 30/70 (RN02),
     * deduz os insumos antes da divisão (RN07) e persiste o repasse do tatuador.
     */
    @Transactional
    public FinalizarSessaoResponse finalizarSessao(Long agendamentoId, BigDecimal custoInsumos, BigDecimal valorTotalCustom) {
        Agendamento agendamento = agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));

        if ("CANCELADO".equalsIgnoreCase(agendamento.getStatus())) {
            throw new BusinessException("Não é possível finalizar uma sessão cancelada");
        }

        if ("AGENDADO".equalsIgnoreCase(agendamento.getStatus())) {
            throw new BusinessException("A finalização da sessão exige que o agendamento tenha sido confirmado previamente com sinal");
        }

        if ("CONCLUIDO".equalsIgnoreCase(agendamento.getStatus())) {
            Optional<Repasse> repasseExistente = repasseRepository.findFirstByAgendamentoIdOrderByDataRepasseDesc(agendamentoId);
            if (repasseExistente.isPresent()) {
                throw new BusinessException("A sessão deste agendamento já foi finalizada e possui repasse registrado");
            }
        }

        BigDecimal total = (valorTotalCustom != null && valorTotalCustom.compareTo(BigDecimal.ZERO) > 0)
                ? valorTotalCustom
                : agendamento.getValorEstimado();

        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor da sessão não foi definido");
        }

        DivisaoSessaoDTO divisao = calcularDivisao(total, custoInsumos);
        divisao.setAgendamentoId(agendamentoId);

        agendamento.setStatus("CONCLUIDO");
        agendamento = agendamentoRepository.save(agendamento);

        Repasse repasse = Repasse.builder()
                .tatuadorId(agendamento.getTatuadorId())
                .agendamentoId(agendamento.getId())
                .valorRepasse(divisao.getValorArtista())
                .valorLiquido(divisao.getValorArtista())
                .custoInsumos(divisao.getCustoInsumos())
                .porcentagemEstudio(divisao.getPorcentagemEstudio())
                .dataRepasse(LocalDateTime.now())
                .status("PENDENTE")
                .build();

        repasse = repasseRepository.save(repasse);

        return FinalizarSessaoResponse.builder()
                .agendamento(agendamento)
                .repasse(repasse)
                .divisao(divisao)
                .build();
    }

    /**
     * Regra RN05: Cálculo de reembolso e cancelamento.
     * Se cancelamento com menos de 48h de antecedência:
     * - Reembolso integral bloqueado.
     * - Aplicação de retenção/desconto (padrão 50% ou percentual informado > 0).
     * Se cancelamento com 48h ou mais de antecedência:
     * - Reembolso integral permitido (100% do valor pago).
     */
    public CalculoReembolsoResponse calcularReembolso(Long agendamentoId,
                                                     LocalDateTime dataReferencia,
                                                     BigDecimal customPercentualDesconto) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));

        LocalDateTime agora = dataReferencia != null ? dataReferencia : LocalDateTime.now();
        LocalDateTime dataSessao = agendamento.getDataHora();

        if (dataSessao == null) {
            throw new BusinessException("O agendamento não possui data/hora definida");
        }

        long horasAntecedencia = Duration.between(agora, dataSessao).toHours();
        boolean menosDe48Horas = horasAntecedencia < 48;
        boolean sessaoPassada = agora.isAfter(dataSessao);

        // Obter total pago pelo cliente (sinal + pagamentos com status CONCLUIDO)
        List<Pagamento> pagamentos = financeiroRepository.findByAgendamentoId(agendamentoId);
        BigDecimal valorPago = pagamentos.stream()
                .filter(p -> "CONCLUIDO".equalsIgnoreCase(p.getStatus()))
                .filter(p -> !"REEMBOLSO".equalsIgnoreCase(p.getTipo()))
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fallback para agendamentos confirmados sem pagamentos persistidos
        if (valorPago.compareTo(BigDecimal.ZERO) == 0 && Set.of("CONFIRMADO", "EM_ANDAMENTO", "CONCLUIDO").contains(agendamento.getStatus())) {
            if (agendamento.getValorEstimado() != null) {
                valorPago = agendamento.getValorEstimado().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
        }

        boolean reembolsoIntegralPermitido;
        BigDecimal percentualDesconto;
        BigDecimal valorDesconto;
        BigDecimal valorReembolsavel;
        String mensagem;

        if (sessaoPassada) {
            reembolsoIntegralPermitido = false;
            percentualDesconto = new BigDecimal("100.00");
            valorDesconto = valorPago;
            valorReembolsavel = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            mensagem = "Sessão já realizada ou horário ultrapassado: cancelamento sem reembolso.";
        } else if (menosDe48Horas) {
            // Regra RN05: < 48h -> Bloqueio de reembolso integral
            reembolsoIntegralPermitido = false;
            BigDecimal taxa = customPercentualDesconto != null ? customPercentualDesconto : new BigDecimal("50.00");
            if (taxa.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Cancelamentos com menos de 48 horas de antecedência não têm direito a reembolso integral (RN05)");
            }
            if (taxa.compareTo(new BigDecimal("100.00")) > 0) {
                throw new BusinessException("O percentual de desconto não pode ser superior a 100%");
            }
            percentualDesconto = taxa.setScale(2, RoundingMode.HALF_UP);
            valorDesconto = valorPago.multiply(percentualDesconto).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            valorReembolsavel = valorPago.subtract(valorDesconto);
            mensagem = String.format("Cancelamento com menos de 48h de antecedência (%d horas restantes): "
                            + "reembolso integral bloqueado (RN05). Retenção de %s%% aplicada.",
                    horasAntecedencia, percentualDesconto.toPlainString());
        } else {
            // Regra RN05: >= 48h -> Reembolso integral permitido
            reembolsoIntegralPermitido = true;
            percentualDesconto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            valorDesconto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            valorReembolsavel = valorPago;
            mensagem = String.format("Cancelamento com antecedência de %d horas (>= 48h): reembolso integral permitido (RN05).",
                    horasAntecedencia);
        }

        return CalculoReembolsoResponse.builder()
                .agendamentoId(agendamentoId)
                .dataAgendamento(dataSessao)
                .dataCancelamento(agora)
                .horasAntecedencia(horasAntecedencia)
                .menosDe48Horas(menosDe48Horas)
                .reembolsoIntegralPermitido(reembolsoIntegralPermitido)
                .valorPago(valorPago)
                .percentualDesconto(percentualDesconto)
                .valorDesconto(valorDesconto)
                .valorReembolsavel(valorReembolsavel)
                .politica("RN05")
                .mensagem(mensagem)
                .build();
    }

    /**
     * Cancela o agendamento aplicando as diretrizes da regra RN05.
     */
    @Transactional
    public CancelamentoResponse cancelarAgendamento(Long agendamentoId, CancelarAgendamentoRequest request) {
        Agendamento agendamento = agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));

        if ("CANCELADO".equalsIgnoreCase(agendamento.getStatus())) {
            throw new BusinessException("Agendamento já se encontra cancelado");
        }

        if ("CONCLUIDO".equalsIgnoreCase(agendamento.getStatus())) {
            throw new BusinessException("Não é possível cancelar uma sessão já concluída");
        }

        LocalDateTime dataCancelamento = (request != null && request.getDataCancelamento() != null)
                ? request.getDataCancelamento()
                : LocalDateTime.now();

        long horas = Duration.between(dataCancelamento, agendamento.getDataHora()).toHours();

        // Validação explícita de bloqueio de reembolso integral quando < 48h (RN05)
        if (horas < 48) {
            if (request != null && Boolean.TRUE.equals(request.getForcarReembolsoIntegral())) {
                throw new BusinessException("Cancelamentos com menos de 48 horas de antecedência não têm direito a reembolso integral (RN05)");
            }
            if (request != null && request.getPercentualDesconto() != null && request.getPercentualDesconto().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Cancelamentos com menos de 48 horas de antecedência não têm direito a reembolso integral (RN05)");
            }
        }

        CalculoReembolsoResponse reembolso = calcularReembolso(agendamentoId, dataCancelamento,
                request != null ? request.getPercentualDesconto() : null);

        agendamento.setStatus("CANCELADO");
        agendamento = agendamentoRepository.save(agendamento);

        Pagamento estorno = null;
        if (reembolso.getValorReembolsavel().compareTo(BigDecimal.ZERO) > 0) {
            estorno = Pagamento.builder()
                    .agendamentoId(agendamentoId)
                    .valor(reembolso.getValorReembolsavel())
                    .tipo("REEMBOLSO")
                    .formaPagamento("PIX")
                    .status("CONCLUIDO")
                    .dataPagamento(dataCancelamento)
                    .build();
            estorno = financeiroRepository.save(estorno);
        }

        return CancelamentoResponse.builder()
                .agendamento(agendamento)
                .reembolso(reembolso)
                .pagamentoReembolso(estorno)
                .build();
    }

    public List<Repasse> listarRepasses() {
        return repasseRepository.findAll();
    }

    public Repasse buscarRepassePorId(Long id) {
        return repasseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repasse não encontrado com ID: " + id));
    }

    public List<Repasse> buscarRepassesPorAgendamento(Long agendamentoId) {
        return repasseRepository.findByAgendamentoId(agendamentoId);
    }

    public List<Repasse> buscarRepassesPorTatuador(Long tatuadorId) {
        return repasseRepository.findByTatuadorId(tatuadorId);
    }
}
