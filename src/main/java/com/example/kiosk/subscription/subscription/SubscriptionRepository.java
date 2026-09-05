package com.example.kiosk.subscription.subscription;

import com.example.kiosk.auth.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findTopByUserOrderByCreatedAtDesc(AppUser user);
}
