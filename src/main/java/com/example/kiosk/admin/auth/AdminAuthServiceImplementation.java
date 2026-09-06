package com.example.kiosk.admin.auth;

import com.example.kiosk.admin.auth.dto.AdminLoginRequest;
import com.example.kiosk.admin.auth.response.AdminLoginResponse;
import com.example.kiosk.admin.Admin;
import com.example.kiosk.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImplementation implements AdminAuthService{
    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        ResponseStatusException invalidAuth = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");

        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> invalidAuth);

        if(!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw invalidAuth;
        }

        String token = jwtService.generateAdminToken(admin);
        return new AdminLoginResponse(token, admin.getId(), admin.getEmail(), admin.getFirstName(), admin.getLastName());
    }
}
