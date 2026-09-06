package com.example.kiosk.admin.auth;

import com.example.kiosk.admin.auth.dto.AdminLoginRequest;
import com.example.kiosk.admin.auth.response.AdminLoginResponse;
import com.example.kiosk.common.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthServiceImplementation authService;

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request){
        return authService.login(request);
    }
}
