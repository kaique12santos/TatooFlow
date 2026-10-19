package com.tattooflow.modules.convite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AceitarConviteRequest(
        @NotBlank String nome,

        @NotBlank
        @Pattern(regexp = "[0-9]{4,6}")
        String pin,

        @NotBlank
        @Size(max = 255)
        String aparelhoId
) {}