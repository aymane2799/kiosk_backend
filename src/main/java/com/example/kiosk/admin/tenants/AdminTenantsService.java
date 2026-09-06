package com.example.kiosk.admin.tenants;

import com.example.kiosk.admin.tenants.dto.AdminCreatePlanRequest;
import com.example.kiosk.admin.tenants.dto.AdminCreateTenantRequest;
import com.example.kiosk.admin.tenants.dto.AdminUpdateTenantRequest;
import com.example.kiosk.admin.tenants.response.TenantAdminResponse;
import com.example.kiosk.auth.user.response.AppUserResponse;
import com.example.kiosk.subscription.plan.response.PlanResponse;

import java.util.List;

public interface AdminTenantsService {
    List<TenantAdminResponse> listTenants();
    TenantAdminResponse createTenant(AdminCreateTenantRequest request);
    TenantAdminResponse updateTenant(String tenantId, AdminUpdateTenantRequest request);

    List<AppUserResponse> listUsers(String tenantId);

    List<PlanResponse> listPlans(String tenantId);
    PlanResponse createPlan(String tenantId, AdminCreatePlanRequest request);
}
