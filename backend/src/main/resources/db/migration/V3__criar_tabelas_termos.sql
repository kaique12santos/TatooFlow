CREATE TABLE termos_aceite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    agendamento_id BIGINT NOT NULL,
    url_pdf_assinado VARCHAR(500) NOT NULL,
    data_assinatura DATETIME,
    ip_assinatura VARCHAR(50)
);
