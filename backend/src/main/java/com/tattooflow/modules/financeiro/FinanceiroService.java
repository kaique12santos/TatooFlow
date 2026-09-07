package com.tattooflow.modules.financeiro;

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
        return financeiroRepository.save(pagamento);
    }
}
