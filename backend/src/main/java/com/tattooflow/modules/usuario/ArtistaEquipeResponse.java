package com.tattooflow.modules.usuario;

public record ArtistaEquipeResponse(
        String id,
        String name,
        String station,
        String specialty,
        String pinLabel,
        String sinceLabel,
        String statusBadge,
        String status,
        String avatar,
        String role,
        String email
) {
    public static ArtistaEquipeResponse from(Usuario usuario) {
        boolean ativo = Boolean.TRUE.equals(usuario.getAtivo());
        String nome = usuario.getNome() == null ? "Artista" : usuario.getNome();
        String encodedName = nome.trim().replace(" ", "+");

        return new ArtistaEquipeResponse(
                String.valueOf(usuario.getId()),
                nome,
                "Sem bancada vinculada",
                "Tatuador",
                ativo ? "Aparelho autorizado" : "PIN bloqueado",
                ativo ? "Ativo" : "Acesso revogado",
                ativo ? "ATIVO" : "REVOGADO",
                ativo ? "ativo" : "revogado",
                "https://ui-avatars.com/api/?name=" + encodedName + "&background=141416&color=e6c383",
                "Tatuador",
                usuario.getEmail()
        );
    }
}
