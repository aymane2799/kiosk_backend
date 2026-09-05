package com.example.kiosk.auth.user;

import com.example.kiosk.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findByTenant(Tenant tenant);
    Optional<AppUser> findByTenantAndExternalId(Tenant tenant, String externalId);
}
