package com.example.kiosk.tenant;

import com.example.kiosk.common.ApiPaths;
import com.example.kiosk.tenant.response.TenantPublicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantServiceImplementation tenantService;

    @GetMapping("{slug}/config")
    public TenantPublicResponse getPublicConfig(@PathVariable String slug) {
        System.out.println(slug);
        return tenantService.getPublicConfig(slug);
    }
}
