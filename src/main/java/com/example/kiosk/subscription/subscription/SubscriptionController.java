package com.example.kiosk.subscription.subscription;

import com.example.kiosk.common.ApiPaths;
import com.example.kiosk.subscription.plan.response.PlanResponse;
import com.example.kiosk.subscription.subscription.dto.CheckoutRequest;
import com.example.kiosk.subscription.subscription.response.SubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1)
@RequiredArgsConstructor
public class SubscriptionController {

    private final CheckoutServiceImplementation checkoutService;

    @GetMapping("/tenants/{slug}/plans")
    public List<PlanResponse> listPlans(@PathVariable String slug) {
        return checkoutService.listPlans(slug);
    }

    @GetMapping("/me/subscription")
    public SubscriptionResponse getCurrentSubscription(Authentication authentication) {
        return checkoutService.getCurrentSubscription(authentication.getName());
    }

    @PostMapping("/subscriptions/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse checkout(Authentication authentication, @Valid  @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(authentication.getName(), request);
    }


}
