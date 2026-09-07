package com.tattooflow.modules.relatorio;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RelatorioService {

    public Map<String, Object> gerarRelatorioMensal(int mes, int ano) {
        Map<String, Object> relatorio = new HashMap<>();
        relatorio.put("mes", mes);
        relatorio.put("ano", ano);
        relatorio.put("totalFaturamento", 0);
        relatorio.put("totalSessoes", 0);
        return relatorio;
    }
}
