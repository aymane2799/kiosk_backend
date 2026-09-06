package com.example.kiosk.subscription.plan;

import com.example.kiosk.subscription.plan.dto.CreatePlanRequest;
import com.example.kiosk.subscription.plan.response.PlanResponse;
import com.example.kiosk.tenant.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImplementation implements PlanService {

    private final PlanRepository planRepository;

    public List<Plan> listPlansForTenantId(String tenantId) {
        return planRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Plan> listPlansForTenant(Tenant tenant) {
        return planRepository.findByTenant(tenant);
    }

    @Override
    public Plan createPlan(Tenant tenant, CreatePlanRequest request) {
        return planRepository.save(Plan.builder()
                .tenant(tenant)
                .name(request.name())
                .tier(request.tier())
                .price(request.price())
                .currency(request.currency())
                .billingPeriod(request.billingPeriod())
                .build());
    }
}
