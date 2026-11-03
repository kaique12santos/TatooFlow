package com.tattooflow.modules.convite;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record CriarConviteRequest(@NotBlank @Email @Size(max = 255) String emailConvidado) {}
