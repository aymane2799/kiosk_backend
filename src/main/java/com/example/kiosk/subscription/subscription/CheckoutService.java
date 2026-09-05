package com.example.kiosk.subscription.subscription;

import com.example.kiosk.subscription.plan.response.PlanResponse;
import com.example.kiosk.subscription.subscription.dto.CheckoutRequest;
import com.example.kiosk.subscription.subscription.response.SubscriptionResponse;

import java.util.List;

public interface CheckoutService {
    List<PlanResponse> listPlans(String slug);
    SubscriptionResponse getCurrentSubscription(String userId);
    SubscriptionResponse checkout(String userId, CheckoutRequest request);

}
