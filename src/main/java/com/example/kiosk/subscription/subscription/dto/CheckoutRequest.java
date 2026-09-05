package com.example.kiosk.subscription.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequest(
        @NotBlank String planId,
        boolean simulateFailure
) {
}
