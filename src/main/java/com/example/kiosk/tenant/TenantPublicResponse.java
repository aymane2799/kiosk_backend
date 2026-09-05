package com.example.kiosk.tenant;

public record TenantPublicResponse(
        String slug,
        String name,
        String logoUrl,
        String primaryColor,
        String secondaryColor,
        String providerId
) {

    public static TenantPublicResponse from (Tenant tenant) {
        return new TenantPublicResponse(
                tenant.getSlug(),
                tenant.getName(),
                tenant.getLogoUrl(),
                tenant.getPrimaryColor(),
                tenant.getSecondaryColor(),
                tenant.getProviderId()
        );
    }
}
