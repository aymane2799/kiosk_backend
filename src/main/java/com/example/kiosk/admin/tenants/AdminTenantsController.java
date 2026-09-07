package com.example.kiosk.admin.tenants;

import com.example.kiosk.admin.tenants.dto.AdminCreatePlanRequest;
import com.example.kiosk.admin.tenants.dto.AdminCreateTenantRequest;
import com.example.kiosk.admin.tenants.dto.AdminUpdateTenantRequest;
import com.example.kiosk.admin.tenants.response.TenantAdminResponse;
import com.example.kiosk.auth.user.response.AppUserResponse;
import com.example.kiosk.common.ApiPaths;
import com.example.kiosk.subscription.plan.response.PlanResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/admin/tenants")
@RequiredArgsConstructor
public class AdminTenantsController {
    private final AdminTenantsServiceImplementation adminTenantService;

    @GetMapping
    public List<TenantAdminResponse> getTenants() {
        return adminTenantService.listTenants();
    }

    @GetMapping("{id}")
    public TenantAdminResponse getTenant(@PathVariable  String id) {
        return adminTenantService.getTenant(id);
    }

    @PostMapping
    public TenantAdminResponse createTenant(@Valid @RequestBody AdminCreateTenantRequest request) {
        return adminTenantService.createTenant(request);
    }

    @PutMapping("{id}")
    public TenantAdminResponse updateTenant(@PathVariable String id, @Valid @RequestBody AdminUpdateTenantRequest request) {
        return adminTenantService.updateTenant(id, request);
    }

//  users
    @GetMapping("{id}/users")
    public List<AppUserResponse> getUsers(@PathVariable String id) {
        return adminTenantService.listUsers(id);
    }

//  plans
    @GetMapping("{id}/plans")
    public List<PlanResponse> getPlans(@PathVariable String id) {
        return adminTenantService.listPlans(id);
    }

    @PostMapping("{id}/plans")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(@PathVariable String id, @Valid @RequestBody AdminCreatePlanRequest request) {
        return adminTenantService.createPlan(id, request);
    }

}
