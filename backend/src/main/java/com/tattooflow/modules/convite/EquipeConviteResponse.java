package com.tattooflow.modules.convite;

import java.time.LocalDateTime;

public record EquipeConviteResponse(
        String id,
        String name,
        String email,
        String phone,
        String role,
        String station,
        String pin,
        String validity,
        String status,
        String statusText,
        LocalDateTime createdAt,
        String token
) {
    public static EquipeConviteResponse from(Convite convite) {
        LocalDateTime agora = LocalDateTime.now();
        boolean utilizado = Boolean.TRUE.equals(convite.getUtilizado());
        boolean expirado = convite.getDataExpiracao() == null || convite.getDataExpiracao().isBefore(agora);
        String status = utilizado ? "active" : expirado ? "expired" : "pending";
        String statusText = utilizado
                ? "Convite utilizado"
                : expirado ? "Convite expirado" : "Aguardando ativação";
        LocalDateTime createdAt = convite.getDataExpiracao() == null
                ? agora
                : convite.getDataExpiracao().minusDays(7);

        return new EquipeConviteResponse(
                String.valueOf(convite.getId()),
                convite.getEmailConvidado(),
                convite.getEmailConvidado(),
                "",
                "Tatuador Convidado",
                "Sem bancada vinculada",
                "",
                "7d",
                status,
                statusText,
                createdAt,
                convite.getCodigo()
        );
    }
}
