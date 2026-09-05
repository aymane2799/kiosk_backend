package com.example.kiosk.entitlement;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.Content;
import com.example.kiosk.content.ContentTier;
import com.example.kiosk.subscription.subscription.Subscription;
import com.example.kiosk.subscription.subscription.SubscriptionRepository;
import com.example.kiosk.subscription.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EntitlementServiceImplementation implements EntitlementService{
    private final SubscriptionRepository subscriptionRepository;

    public boolean hasAccess(AppUser user, Content content) {
        if (content.getTier() == ContentTier.FREE) {
            return true;
        }

        return subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .filter(this::isActivePremium)
                .isPresent();
    }

    private boolean isActivePremium(Subscription subscription) {
        return subscription.getStatus() == SubscriptionStatus.ACTIVE
                && subscription.getPlan().getTier() == ContentTier.PREMIUM
                && (subscription.getExpiresAt() == null || subscription.getExpiresAt().isAfter(Instant.now()));
    }
}
