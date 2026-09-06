package com.example.kiosk.admin.auth;

import com.example.kiosk.admin.auth.dto.AdminLoginRequest;
import com.example.kiosk.admin.auth.response.AdminLoginResponse;

public interface AdminAuthService {
    AdminLoginResponse login(AdminLoginRequest request);
}
