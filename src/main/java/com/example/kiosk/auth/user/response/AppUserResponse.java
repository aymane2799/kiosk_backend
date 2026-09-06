package com.example.kiosk.auth.user.response;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.auth.user.Role;

import java.time.Instant;

public record AppUserResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Instant createdAt
) {
    public static AppUserResponse from(AppUser appUser) {
        return new AppUserResponse(
                appUser.getId(),
                appUser.getFirstName(),
                appUser.getLastName(),
                appUser.getEmail(),
                appUser.getRole(),
                appUser.getCreatedAt()
        );
    }
}
