package com.tattooflow.modules.termo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termos_aceite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermoAceite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "agendamento_id", nullable = false)
    private Long agendamentoId;

    @Column(name = "url_pdf_assinado", nullable = false)
    private String urlPdfAssinado;

    @Column(name = "data_assinatura")
    private LocalDateTime dataAssinatura;

    @Column(name = "ip_assinatura")
    private String ipAssinatura;

    @Column(name = "data_expiracao_retencao")
    private LocalDateTime dataExpiracaoRetencao;
}
