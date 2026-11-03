package com.tattooflow.modules.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "PIN é obrigatório")
        @Pattern(regexp = "[0-9]{4,6}", message = "PIN deve conter de 4 a 6 dígitos")
        @Schema(description = "PIN como texto, preservando zeros iniciais", example = "012345",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        String pin,

        @NotBlank(message = "ID do aparelho é obrigatório")
        @Size(max = 255, message = "ID do aparelho deve ter no máximo 255 caracteres")
        @Schema(description = "ID do aparelho previamente autorizado para o usuário", example = "aparelho-tatuador-01")
        String aparelhoId
) {
    @Override
    public String toString() {
        return "LoginRequest[pin=***, aparelhoId=" + aparelhoId + "]";
    }
}
