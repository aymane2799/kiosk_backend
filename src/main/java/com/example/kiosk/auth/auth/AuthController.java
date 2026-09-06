package com.example.kiosk.auth.auth;

import com.example.kiosk.auth.auth.dto.LoginRequest;
import com.example.kiosk.auth.auth.dto.MockPartnerTokenRequest;
import com.example.kiosk.auth.auth.response.LoginResponse;
import com.example.kiosk.auth.auth.response.MockPartnerTokenResponse;
import com.example.kiosk.common.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthServiceImplementation authService;

    @PostMapping("login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("mock-partner-token")
    public MockPartnerTokenResponse mockPartnerToken(@Valid @RequestBody MockPartnerTokenRequest request) {
        return authService.generatePartnerToken(request);
    }
}
