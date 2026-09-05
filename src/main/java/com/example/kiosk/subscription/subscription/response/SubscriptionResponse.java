package com.example.kiosk.subscription.subscription.response;

import com.example.kiosk.content.ContentTier;
import com.example.kiosk.subscription.plan.Plan;
import com.example.kiosk.subscription.subscription.Subscription;
import com.example.kiosk.subscription.subscription.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionResponse(
        String id,
        String planId,
        String planName,
        ContentTier planTier,
        SubscriptionStatus status,
        Instant startedAt,
        Instant expiresAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getPlan().getTier(),
                subscription.getStatus(),
                subscription.getStartedAt(),
                subscription.getExpiresAt()
        );
    }
}
