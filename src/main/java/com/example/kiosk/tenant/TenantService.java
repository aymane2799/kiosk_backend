package com.example.kiosk.tenant;

import com.example.kiosk.admin.tenants.dto.AdminCreateTenantRequest;
import com.example.kiosk.tenant.response.TenantPublicResponse;

import java.util.List;

public interface TenantService {
    TenantPublicResponse getPublicConfig(String slug);
    List<Tenant> listAll();
    Tenant createTenant(AdminCreateTenantRequest request);
    Tenant requireTenant(String id);

    private void requireSlugAndNameAvailable(AdminCreateTenantRequest request, String id) {}
}
