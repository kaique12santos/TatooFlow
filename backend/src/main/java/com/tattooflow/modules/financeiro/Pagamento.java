package com.tattooflow.modules.financeiro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long agendamentoId;

    @Column(nullable = false)
    private BigDecimal valor;

    private String formaPagamento; // PIX, CARTAO, DINHEIRO
    private String status; // PENDENTE, CONCLUIDO
    private LocalDateTime dataPagamento;

    @Builder.Default
    @Column(nullable = false)
    private String tipo = "PAGAMENTO"; // PAGAMENTO, SINAL

    private String comprovanteNome;
    private String comprovanteContentType;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] comprovante;
}
