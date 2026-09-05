package com.example.kiosk.auth.auth;

public record PartnerIdentity(
        String providerId,
        String externalId,
        String email,
        String firstName,
        String lastName
) {
}
