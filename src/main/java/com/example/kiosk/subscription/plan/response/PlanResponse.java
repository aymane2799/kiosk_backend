package com.example.kiosk.subscription.plan.response;

import com.example.kiosk.content.ContentTier;
import com.example.kiosk.subscription.plan.Plan;

public record PlanResponse(
        String id,
        String name,
        ContentTier tier,
        int price,
        String currency,
        int billingPeriod
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getTier(),
                plan.getPrice(),
                plan.getCurrency(),
                plan.getBillingPeriod()
        );
    }
}
