package com.example.kiosk.auth.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String tenantSlug,
        @NotBlank String partnerToken
) {
}
