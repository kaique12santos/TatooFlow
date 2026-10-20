package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import com.tattooflow.common.exception.ResourceNotFoundException;
import com.tattooflow.modules.agendamento.Agendamento;
import com.tattooflow.modules.agendamento.AgendamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Service
public class SinalService {
    private static final long LIMITE_COMPROVANTE = 5 * 1024 * 1024;
    private final AgendamentoRepository agendamentoRepository;
    private final FinanceiroRepository financeiroRepository;

    public SinalService(AgendamentoRepository agendamentoRepository, FinanceiroRepository financeiroRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.financeiroRepository = financeiroRepository;
    }

    @Transactional
    public Pagamento registrar(Long agendamentoId, BigDecimal valor, String formaPagamento, MultipartFile comprovante) {
        // Compartilha o bloqueio com a edição do agendamento e impede sinais concorrentes.
        Agendamento agendamento = agendamentoRepository.buscarPorIdParaAtualizacao(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));
        if (financeiroRepository.existsByAgendamentoIdAndTipo(agendamentoId, "SINAL")) {
            throw new BusinessException("O sinal deste agendamento já foi registrado");
        }
        if (!"AGENDADO".equals(agendamento.getStatus())) {
            throw new BusinessException("Somente agendamentos com status AGENDADO podem receber o sinal");
        }
        BigDecimal total = agendamento.getValorEstimado();
        if (total == null || total.signum() <= 0 || total.stripTrailingZeros().scale() > 2) {
            throw new BusinessException("O agendamento deve ter valor estimado positivo com no máximo duas casas decimais");
        }
        BigDecimal valorSinal = total.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        if (valor == null || valor.compareTo(valorSinal) != 0) {
            throw new BusinessException("O sinal deve corresponder a 50% do valor estimado: " + valorSinal);
        }
        if (formaPagamento == null || !Set.of("PIX", "CARTAO", "DINHEIRO").contains(formaPagamento)) {
            throw new BusinessException("Forma de pagamento inválida. Use PIX, CARTAO ou DINHEIRO");
        }

        byte[] conteudo = lerComprovante(comprovante);
        String contentType = identificarContentType(conteudo);
        String tipoInformado = comprovante.getContentType();
        if (tipoInformado != null && !"application/octet-stream".equalsIgnoreCase(tipoInformado)
                && !contentType.equals(tipoInformado.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("O conteúdo do comprovante não corresponde ao tipo de arquivo informado");
        }
        Pagamento sinal = financeiroRepository.save(Pagamento.builder()
                .agendamentoId(agendamentoId).valor(valorSinal).tipo("SINAL")
                .formaPagamento(formaPagamento).status("CONCLUIDO").dataPagamento(LocalDateTime.now())
                .comprovanteNome(nomeComprovante(comprovante.getOriginalFilename(), contentType))
                .comprovanteContentType(contentType).comprovante(conteudo).build());
        agendamento.setStatus("CONFIRMADO");
        agendamentoRepository.save(agendamento);
        return sinal;
    }

    @Transactional(readOnly = true)
    public ComprovanteSinal buscarComprovante(Long agendamentoId) {
        Pagamento sinal = financeiroRepository.findByAgendamentoIdAndTipo(agendamentoId, "SINAL")
                .orElseThrow(() -> new ResourceNotFoundException("Comprovante de sinal não encontrado para o agendamento: " + agendamentoId));
        if (sinal.getComprovante() == null) {
            throw new ResourceNotFoundException("Comprovante de sinal não encontrado para o agendamento: " + agendamentoId);
        }
        return new ComprovanteSinal(sinal.getComprovanteNome(), sinal.getComprovanteContentType(), sinal.getComprovante());
    }

    private byte[] lerComprovante(MultipartFile comprovante) {
        if (comprovante == null || comprovante.isEmpty()) {
            throw new BusinessException("O comprovante do sinal é obrigatório");
        }
        if (comprovante.getSize() > LIMITE_COMPROVANTE) {
            throw new BusinessException("O comprovante deve ter no máximo 5 MB");
        }
        try {
            return comprovante.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler o comprovante do sinal");
        }
    }

    private String identificarContentType(byte[] conteudo) {
        if (comecaCom(conteudo, new byte[]{'%', 'P', 'D', 'F', '-'})) {
            return "application/pdf";
        }
        if (comecaCom(conteudo, new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10})) {
            return "image/png";
        }
        if (comecaCom(conteudo, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff})) {
            return "image/jpeg";
        }
        throw new BusinessException("O comprovante deve ser um arquivo PDF, PNG ou JPEG");
    }

    private boolean comecaCom(byte[] conteudo, byte[] assinatura) {
        return conteudo.length >= assinatura.length
                && Arrays.equals(Arrays.copyOf(conteudo, assinatura.length), assinatura);
    }

    private String nomeComprovante(String nomeOriginal, String contentType) {
        String extensao = switch (contentType) {
            case "application/pdf" -> "pdf";
            case "image/png" -> "png";
            default -> "jpg";
        };
        String nome = nomeOriginal == null ? "" : nomeOriginal.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
        return nome.isBlank() ? "comprovante." + extensao : nome.substring(0, Math.min(nome.length(), 255));
    }
}
