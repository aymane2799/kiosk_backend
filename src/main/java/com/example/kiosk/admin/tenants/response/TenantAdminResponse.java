package com.example.kiosk.admin.tenants.response;

import com.example.kiosk.tenant.Tenant;

import java.time.Instant;

public record TenantAdminResponse(
        String id,
        String slug,
        String name,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String providerId,
        Instant createdAt
) {
    public static TenantAdminResponse from(Tenant tenant) {
        return new TenantAdminResponse(
                tenant.getId(),
                tenant.getSlug(),
                tenant.getName(),
                tenant.getLogoUrl(),
                tenant.getPrimaryColor(),
                tenant.getSecondaryColor(),
                tenant.getProviderId(),
                tenant.getCreatedAt()
        );
    }
}
