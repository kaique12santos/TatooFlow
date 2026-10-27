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

import java.sql.ResultSet;
import java.util.List;

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
    @DisplayName("Deve validar que todas as migrations do Flyway foram aplicadas com sucesso até a V11")
    void deveValidarVersaoMaisAtualizadaDoFlyway() {
        MigrationInfo[] migrations = flyway.info().all();
        assertThat(migrations).isNotEmpty();

        MigrationInfo latest = flyway.info().current();
        assertThat(latest).isNotNull();
        assertThat(latest.getVersion().getVersion()).isEqualTo("11");
        assertThat(latest.getDescription()).containsIgnoringCase("triagem");
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
}
