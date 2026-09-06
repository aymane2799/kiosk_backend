package com.example.kiosk.subscription.plan;

import com.example.kiosk.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, String> {
    List<Plan> findByTenant(Tenant tenant);
    List<Plan> findByTenantId(String tenant);
}
