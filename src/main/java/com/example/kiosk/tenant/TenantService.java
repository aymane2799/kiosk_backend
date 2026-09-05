package com.example.kiosk.tenant;

public interface TenantService {
    TenantPublicResponse getPublicConfig(String slug);
}
