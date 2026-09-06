package com.example.kiosk.admin.tenants.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminCreateTenantRequest(
        @NotBlank String name,
        @NotBlank String slug,
        @NotBlank String logoUrl,
        @NotBlank String primaryColor,
        @NotBlank String secondaryColor,
        @NotBlank String providerId
) {
}
