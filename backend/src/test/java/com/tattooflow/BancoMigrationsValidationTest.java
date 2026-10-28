package com.tattooflow;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:migration_val_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("h2")
@Transactional
class BancoMigrationsValidationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve validar que todas as migrations do Flyway foram aplicadas com sucesso até a V13")
    void deveValidarVersaoMaisAtualizadaDoFlyway() {
        MigrationInfo[] migrations = flyway.info().all();
        assertThat(migrations).isNotEmpty();

        MigrationInfo latest = flyway.info().current();
        assertThat(latest).isNotNull();
        assertThat(latest.getVersion().getVersion()).isEqualTo("13");
        assertThat(latest.getDescription()).containsIgnoringCase("script");
        assertThat(latest.getState().isApplied()).isTrue();
    }

    @Test
    @DisplayName("[F2-BANCO-01] Validar estrutura da tabela pre_agendamentos (referência, local, data desejada, status, cliente_id)")
    void deveValidarEstruturaPreAgendamentos() {
        List<String> colunas = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'PRE_AGENDAMENTOS'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunas).contains(
                "id",
                "cliente_id",
                "referencia",
                "local_corpo",
                "data_desejada",
                "status_triagem"
        );
    }

    @Test
    @DisplayName("[F2-BANCO-01] Validar relacionamento e integridade referencial de pre_agendamentos com clientes")
    void deveValidarIntegridadeReferencialPreAgendamentosCliente() {
        // Criar cliente válido
        jdbcTemplate.update("INSERT INTO clientes (nome, telefone) VALUES ('Cliente Teste', '11999991111')");
        Long clienteId = jdbcTemplate.queryForObject("SELECT id FROM clientes WHERE telefone = '11999991111'", Long.class);

        // Inserção com cliente válido deve funcionar
        jdbcTemplate.update(
                "INSERT INTO pre_agendamentos (cliente_id, referencia, local_corpo, data_desejada, status_triagem) VALUES (?, ?, ?, ?, ?)",
                clienteId, "Referencia dragao oriental", "Antebraco", "Proximo sabado", "PENDENTE"
        );

        // Inserção com cliente inexistente deve violar integridade referencial
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO pre_agendamentos (cliente_id, referencia, local_corpo, data_desejada, status_triagem) VALUES (?, ?, ?, ?, ?)",
                999999L, "Referencia", "Braco", "Amanha", "PENDENTE"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("[F3-BANCO-01] Validar estrutura da tabela termos_aceite (cliente, agendamento, pdf, assinatura, IP, expiracao retencao RN06)")
    void deveValidarEstruturaTermosAceite() {
        List<String> colunas = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'TERMOS_ACEITE'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunas).contains(
                "id",
                "cliente_id",
                "agendamento_id",
                "url_pdf_assinado",
                "data_assinatura",
                "ip_assinatura",
                "data_expiracao_retencao"
        );
    }

    @Test
    @DisplayName("[F3-BANCO-01] Validar integridade referencial de termos_aceite com clientes e agendamentos")
    void deveValidarIntegridadeReferencialTermosAceite() {
        // Criar cliente e agendamento válidos
        jdbcTemplate.update("INSERT INTO clientes (nome, telefone) VALUES ('Cliente Termo', '11988882222')");
        Long clienteId = jdbcTemplate.queryForObject("SELECT id FROM clientes WHERE telefone = '11988882222'", Long.class);

        jdbcTemplate.update(
                "INSERT INTO agendamentos (cliente_id, tatuador_id, data_hora, status, duracao_minutos) VALUES (?, 1, NOW(), 'CONFIRMADO', 60)",
                clienteId
        );
        Long agendamentoId = jdbcTemplate.queryForObject("SELECT id FROM agendamentos WHERE cliente_id = ?", Long.class, clienteId);

        // Inserção válida deve funcionar
        jdbcTemplate.update(
                "INSERT INTO termos_aceite (cliente_id, agendamento_id, url_pdf_assinado, data_assinatura, ip_assinatura, data_expiracao_retencao) VALUES (?, ?, ?, NOW(), '127.0.0.1', DATEADD('YEAR', 5, NOW()))",
                clienteId, agendamentoId, "http://exemplo.com/termo.pdf"
        );

        // Inserção com agendamento inexistente deve falhar
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO termos_aceite (cliente_id, agendamento_id, url_pdf_assinado, data_assinatura, ip_assinatura, data_expiracao_retencao) VALUES (?, ?, ?, NOW(), '127.0.0.1', DATEADD('YEAR', 5, NOW()))",
                clienteId, 999999L, "http://exemplo.com/termo.pdf"
        )).isInstanceOf(DataIntegrityViolationException.class);

        // Inserção com cliente inexistente deve falhar
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO termos_aceite (cliente_id, agendamento_id, url_pdf_assinado, data_assinatura, ip_assinatura, data_expiracao_retencao) VALUES (?, ?, ?, NOW(), '127.0.0.1', DATEADD('YEAR', 5, NOW()))",
                999999L, agendamentoId, "http://exemplo.com/termo.pdf"
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("[F5-BANCO-01] Validar estrutura da tabela agendamentos e índices para consulta por data e status")
    void deveValidarEstruturaEIndicesAgendamentos() {
        List<String> colunas = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'AGENDAMENTOS'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunas).contains(
                "id",
                "cliente_id",
                "tatuador_id",
                "data_hora",
                "status",
                "duracao_minutos",
                "descricao_sessao",
                "valor_estimado"
        );

        List<String> indices = jdbcTemplate.query(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE UPPER(TABLE_NAME) = 'AGENDAMENTOS'",
                (ResultSet rs, int rowNum) -> rs.getString("INDEX_NAME").toLowerCase()
        );

        assertThat(indices).anyMatch(name -> name.contains("idx_agendamentos_status_data"));
        assertThat(indices).anyMatch(name -> name.contains("idx_agendamentos_data_hora"));

        // Validar persistência e consulta por data e status (confirmado/pendente/finalizado)
        jdbcTemplate.update(
                "INSERT INTO agendamentos (cliente_id, tatuador_id, data_hora, status, duracao_minutos, valor_estimado) VALUES (?, ?, NOW(), ?, ?, ?)",
                10L, 5L, "CONFIRMADO", 120, new BigDecimal("450.00")
        );
        jdbcTemplate.update(
                "INSERT INTO agendamentos (cliente_id, tatuador_id, data_hora, status, duracao_minutos, valor_estimado) VALUES (?, ?, NOW(), ?, ?, ?)",
                11L, 5L, "PENDENTE", 60, new BigDecimal("250.00")
        );
        jdbcTemplate.update(
                "INSERT INTO agendamentos (cliente_id, tatuador_id, data_hora, status, duracao_minutos, valor_estimado) VALUES (?, ?, NOW(), ?, ?, ?)",
                12L, 5L, "FINALIZADO", 90, new BigDecimal("600.00")
        );

        Integer confirmados = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE status = 'CONFIRMADO' AND data_hora IS NOT NULL",
                Integer.class
        );
        assertThat(confirmados).isGreaterThanOrEqualTo(1);

        Integer pendentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE status = 'PENDENTE'",
                Integer.class
        );
        assertThat(pendentes).isGreaterThanOrEqualTo(1);

        Integer finalizados = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE status = 'FINALIZADO'",
                Integer.class
        );
        assertThat(finalizados).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("[F6-BANCO-01] Validar tabelas financeiras (pagamentos, repasses, comissões e relacionamento com agendamentos)")
    void deveValidarTabelasFinanceiras() {
        // 1. Tabela pagamentos (sinal, valor total, status, relacionamento com agendamentos)
        List<String> colunasPagamentos = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'PAGAMENTOS'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunasPagamentos).contains(
                "id",
                "agendamento_id",
                "valor",
                "status",
                "tipo"
        );

        // 2. Tabela repasses (artista, valor líquido, comissão, data, relacionamento com agendamento)
        List<String> colunasRepasses = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'REPASSES'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunasRepasses).contains(
                "id",
                "tatuador_id",
                "agendamento_id",
                "valor_repasse",
                "valor_liquido",
                "custo_insumos",
                "porcentagem_estudio",
                "data_repasse",
                "status"
        );

        List<String> indicesRepasses = jdbcTemplate.query(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE UPPER(TABLE_NAME) = 'REPASSES'",
                (ResultSet rs, int rowNum) -> rs.getString("INDEX_NAME").toLowerCase()
        );
        assertThat(indicesRepasses).anyMatch(name -> name.contains("idx_repasses_agendamento"));
        assertThat(indicesRepasses).anyMatch(name -> name.contains("idx_repasses_tatuador_data"));

        // 3. Teste de inserção e relacionamento com agendamentos
        jdbcTemplate.update(
                "INSERT INTO agendamentos (cliente_id, tatuador_id, data_hora, status, duracao_minutos) VALUES (?, ?, NOW(), 'CONFIRMADO', 60)",
                100L, 20L
        );
        Long agendamentoId = jdbcTemplate.queryForObject(
                "SELECT id FROM agendamentos WHERE cliente_id = 100 AND tatuador_id = 20 ORDER BY id DESC LIMIT 1",
                Long.class
        );

        // Pagamento de Sinal
        jdbcTemplate.update(
                "INSERT INTO pagamentos (agendamento_id, valor, status, tipo, data_pagamento) VALUES (?, ?, ?, ?, NOW())",
                agendamentoId, new BigDecimal("150.00"), "CONCLUIDO", "SINAL"
        );

        // Pagamento restante
        jdbcTemplate.update(
                "INSERT INTO pagamentos (agendamento_id, valor, status, tipo, data_pagamento) VALUES (?, ?, ?, ?, NOW())",
                agendamentoId, new BigDecimal("350.00"), "CONCLUIDO", "PAGAMENTO"
        );

        // Repasse vinculado ao agendamento, com cálculo de taxa de estúdio, custos de insumo e valor líquido
        jdbcTemplate.update(
                "INSERT INTO repasses (tatuador_id, agendamento_id, valor_repasse, valor_liquido, custo_insumos, porcentagem_estudio, data_repasse, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)",
                20L, agendamentoId, new BigDecimal("350.00"), new BigDecimal("320.00"), new BigDecimal("30.00"), new BigDecimal("30.00"), "PAGO"
        );

        Map<String, Object> repasseConsultado = jdbcTemplate.queryForMap(
                "SELECT * FROM repasses WHERE agendamento_id = ?",
                agendamentoId
        );
        assertThat(repasseConsultado.get("AGENDAMENTO_ID")).isEqualTo(agendamentoId);
        assertThat(new BigDecimal(repasseConsultado.get("VALOR_LIQUIDO").toString())).isEqualByComparingTo("320.00");
    }

    @Test
    @DisplayName("[F7-BANCO-01] Validar tabela e view de estoque de insumos (item, quantidade, quantidade mínima) sem duplicação")
    void deveValidarTabelaEViewEstoque() {
        // Validar colunas da tabela estoque_itens
        List<String> colunasItens = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'ESTOQUE_ITENS'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunasItens).contains(
                "id",
                "nome",
                "quantidade",
                "quantidade_minima",
                "categoria",
                "unidade_medida"
        );

        // Validar colunas da VIEW estoque (item, quantidade, quantidade_minima)
        List<String> colunasView = jdbcTemplate.query(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = 'ESTOQUE'",
                (ResultSet rs, int rowNum) -> rs.getString("COLUMN_NAME").toLowerCase()
        );

        assertThat(colunasView).contains(
                "id",
                "item",
                "quantidade",
                "quantidade_minima"
        );

        // Inserir item em estoque_itens e verificar que reflete automaticamente na view estoque
        jdbcTemplate.update(
                "INSERT INTO estoque_itens (nome, quantidade, quantidade_minima, categoria, unidade_medida) VALUES (?, ?, ?, ?, ?)",
                "Agulha 05RL Cheyenne", 8, 15, "AGULHAS", "UNIDADES"
        );

        Map<String, Object> itemConsultado = jdbcTemplate.queryForMap(
                "SELECT id, item, quantidade, quantidade_minima FROM estoque WHERE item = ?",
                "Agulha 05RL Cheyenne"
        );

        assertThat(itemConsultado.get("ITEM")).isEqualTo("Agulha 05RL Cheyenne");
        assertThat((Integer) itemConsultado.get("QUANTIDADE")).isEqualTo(8);
        assertThat((Integer) itemConsultado.get("QUANTIDADE_MINIMA")).isEqualTo(15);

        // Testar consulta de alerta de estoque crítico (quantidade <= quantidade_minima)
        Integer itensCriticos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM estoque WHERE quantidade <= quantidade_minima",
                Integer.class
        );
        assertThat(itensCriticos).isGreaterThanOrEqualTo(1);
    }
}
