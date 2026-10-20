package com.tattooflow.modules.financeiro;

import com.tattooflow.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceiroService {

    private final FinanceiroRepository financeiroRepository;

    public FinanceiroService(FinanceiroRepository financeiroRepository) {
        this.financeiroRepository = financeiroRepository;
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
}
