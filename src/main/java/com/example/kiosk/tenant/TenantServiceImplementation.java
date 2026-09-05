package com.example.kiosk.tenant;

import com.example.kiosk.tenant.response.TenantPublicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class TenantServiceImplementation implements TenantService {
    private final TenantRepository tenantRepository;

    public TenantPublicResponse getPublicConfig(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found ! "));

        return TenantPublicResponse.from(tenant);
    }
}
