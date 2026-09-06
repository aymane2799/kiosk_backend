package com.example.kiosk.tenant;

import com.example.kiosk.admin.tenants.dto.AdminCreateTenantRequest;
import com.example.kiosk.admin.tenants.dto.AdminUpdateTenantRequest;
import com.example.kiosk.tenant.response.TenantPublicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TenantServiceImplementation implements TenantService {
    private final TenantRepository tenantRepository;

    public TenantPublicResponse getPublicConfig(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found ! "));

        return TenantPublicResponse.from(tenant);
    }

    public List<Tenant> listAll() {
        return tenantRepository.findAll();
    }
    
    public Tenant createTenant(AdminCreateTenantRequest request){
        requireSlugAndNameAvailable(request.slug(), request.name(), null);

        return tenantRepository.save(Tenant.builder()
                .name(request.name())
                .slug(request.slug())
                .logoUrl(request.logoUrl())
                .primaryColor(request.primaryColor())
                .secondaryColor(request.secondaryColor())
                .providerId(request.providerId())
                .build());
    }

    public  Tenant updateTenant(String tenantId, AdminUpdateTenantRequest request){
        Tenant tenant = requireTenant(tenantId);
        requireSlugAndNameAvailable(request.slug(), tenant.getName(), tenantId);

        tenant.setName(request.name());
        tenant.setLogoUrl(request.logoUrl());
        tenant.setPrimaryColor(request.primaryColor());
        tenant.setSecondaryColor(request.secondaryColor());
        tenant.setProviderId(request.providerId());

        return tenantRepository.save(tenant);
    }

    public Tenant requireTenant(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found ! "));
    }
    
    public void requireSlugAndNameAvailable(String slug, String name, String id) {
        tenantRepository.findBySlug(slug)
                .filter(tenant -> !tenant.getId().equals(id))
                .ifPresent(tenant -> {
                    throw  new ResponseStatusException(HttpStatus.CONFLICT, "Tenant slug already exists ! ");
                });
        
        tenantRepository.findByName(name)
                .filter(tenant -> !tenant.getId().equals(id))
                .ifPresent(tenant -> {
                    throw  new ResponseStatusException(HttpStatus.CONFLICT, "Tenant name already exists ! ");
                });
    }
}


