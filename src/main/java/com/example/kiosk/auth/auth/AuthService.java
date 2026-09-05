package com.example.kiosk.auth.auth;

import com.example.kiosk.auth.auth.dto.LoginRequest;
import com.example.kiosk.auth.auth.response.LoginResponse;
import com.example.kiosk.auth.user.AppUser;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    AppUser requireUser(String userId);

    private PartnerIdentity decodePartnerIdentity(String partnerToken) {
        return null;
    }
}
