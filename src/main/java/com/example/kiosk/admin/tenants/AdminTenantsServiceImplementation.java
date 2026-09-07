package com.example.kiosk.admin.tenants;

import com.example.kiosk.admin.tenants.dto.AdminCreatePlanRequest;
import com.example.kiosk.admin.tenants.dto.AdminCreateTenantRequest;
import com.example.kiosk.admin.tenants.dto.AdminUpdateTenantRequest;
import com.example.kiosk.admin.tenants.response.TenantAdminResponse;
import com.example.kiosk.auth.user.AppUserServiceImplementation;
import com.example.kiosk.auth.user.response.AppUserResponse;
import com.example.kiosk.subscription.plan.Plan;
import com.example.kiosk.subscription.plan.PlanServiceImplementation;
import com.example.kiosk.subscription.plan.dto.CreatePlanRequest;
import com.example.kiosk.subscription.plan.response.PlanResponse;
import com.example.kiosk.tenant.Tenant;
import com.example.kiosk.tenant.TenantServiceImplementation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTenantsServiceImplementation implements AdminTenantsService {
    private final TenantServiceImplementation tenantService;
    private final AppUserServiceImplementation userService;
    private final PlanServiceImplementation planService;

    @Override
    public List<TenantAdminResponse> listTenants() {
        return tenantService.listAll().stream()
                .map(TenantAdminResponse::from)
                .toList();
    }

    @Override
    public TenantAdminResponse getTenant(String id) {
        Tenant tenant =  tenantService.getById(id);

        return TenantAdminResponse.from(tenant);
    }

    @Override
    public TenantAdminResponse createTenant(AdminCreateTenantRequest request) {
        Tenant tenant = tenantService.createTenant(request);

        return TenantAdminResponse.from(tenant);
    }

    @Override
    public TenantAdminResponse updateTenant(String tenantId, AdminUpdateTenantRequest request) {
        Tenant tenant = tenantService.updateTenant(tenantId, request);

        return TenantAdminResponse.from(tenant);
    }

    @Override
    public List<AppUserResponse> listUsers(String tenantId) {
        System.out.println("tenantId : " + tenantId);
        return userService.listAll(tenantId).stream()
                .map(AppUserResponse::from)
                .toList();
    }

    @Override
    public List<PlanResponse> listPlans(String tenantId) {
        return planService.listPlansForTenantId(tenantId).stream()
                .map(PlanResponse::from)
                .toList();
    }

    @Override
    public PlanResponse createPlan(String tenantId , AdminCreatePlanRequest request) {
        Tenant tenant = tenantService.requireTenant(tenantId);

        CreatePlanRequest createPlanRequest =  new CreatePlanRequest(
                request.name(),
                request.tier(),
                request.price(),
                request.currency(),
                request.billingPeriod()
        );

        Plan plan =  planService.createPlan(tenant, createPlanRequest);

        return PlanResponse.from(plan);
    }
}
