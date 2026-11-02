package com.tattooflow.modules.estoque;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlocacaoEstoqueRequest {
    private String sessionId;
    private String clientId;
    private String allocatedAt;
    private List<AlocacaoItemPayload> items;
}

