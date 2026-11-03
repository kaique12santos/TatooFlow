package com.tattooflow.modules.termo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermoAssinaturaResponse {

    private boolean success;

    private boolean fromBackend;

    private String message;

    private String protocol;

    private String pdfUrl;

    private Long termoId;
}

