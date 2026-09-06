package com.example.kiosk.auth.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MockPartnerTokenRequest(
        @NotBlank String tenantSlug,
        @NotBlank String externalId,
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName
) {
}
