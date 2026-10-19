package com.tattooflow.integrations.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SimularMensagemRequest(
        @NotBlank(message = "Telefone e obrigatorio")
        @Pattern(regexp = "[1-9][0-9]{7,14}",
                message = "Telefone deve conter de 8 a 15 digitos com DDI, sem + ou separadores")
        String telefone,

        @NotBlank(message = "Texto e obrigatorio")
        @Size(max = 4096, message = "Texto deve ter no maximo 4096 caracteres")
        String texto) {
}
