package com.tattooflow.modules.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email,

        @NotBlank(message = "PIN é obrigatório")
        @Pattern(regexp = "[0-9]{4,6}", message = "PIN deve conter de 4 a 6 dígitos")
        @Schema(example = "012345", accessMode = Schema.AccessMode.WRITE_ONLY)
        String pin,

        @NotBlank(message = "ID do aparelho é obrigatório")
        @Size(max = 255, message = "ID do aparelho deve ter no máximo 255 caracteres")
        String aparelhoId,

        @NotBlank(message = "Perfil é obrigatório")
        @Pattern(regexp = "ADMIN|TATUADOR", message = "Perfil deve ser ADMIN ou TATUADOR")
        String perfil
) {
    @Override
    public String toString() {
        return "CriarUsuarioRequest[nome=" + nome + ", email=" + email
                + ", pin=***, aparelhoId=" + aparelhoId + ", perfil=" + perfil + "]";
    }
}
