package com.example.kiosk.subscription.plan;

import com.example.kiosk.subscription.plan.dto.CreatePlanRequest;
import com.example.kiosk.tenant.Tenant;

import java.util.List;

public interface PlanService {
    List<Plan> listPlansForTenantId(String tenant);
    List<Plan> listPlansForTenant(Tenant tenant);
    Plan createPlan(Tenant tenant, CreatePlanRequest request);
}
