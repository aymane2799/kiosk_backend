package com.example.kiosk.tenant;

import com.example.kiosk.tenant.response.TenantPublicResponse;

public interface TenantService {
    TenantPublicResponse getPublicConfig(String slug);
}
