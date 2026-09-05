package com.example.kiosk.auth.auth;

import com.example.kiosk.auth.user.Role;

public record LoginResponse (
        String token,
        String userId,
        String tenantId,
        Role role
){
}
