package com.example.kiosk.subscription.plan.dto;

import com.example.kiosk.content.ContentTier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePlanRequest(
        @NotBlank String name,
        @NotNull ContentTier tier,
        @Min(0) int price,
        @NotBlank String currency,
        @Positive int billingPeriod) {
}
