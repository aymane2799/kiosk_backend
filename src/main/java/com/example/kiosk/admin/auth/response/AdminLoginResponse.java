package com.example.kiosk.admin.auth.response;

public record AdminLoginResponse(
        String token,
        String id,
        String email,
        String firstName,
        String lastName
) {
}
