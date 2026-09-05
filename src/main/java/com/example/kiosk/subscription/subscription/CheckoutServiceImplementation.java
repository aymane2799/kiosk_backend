package com.example.kiosk.subscription.subscription;

import com.example.kiosk.auth.auth.AuthServiceImplementation;
import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.subscription.payment.Payment;
import com.example.kiosk.subscription.payment.PaymentRepository;
import com.example.kiosk.subscription.payment.PaymentStatus;
import com.example.kiosk.subscription.plan.Plan;
import com.example.kiosk.subscription.plan.PlanRepository;
import com.example.kiosk.subscription.plan.response.PlanResponse;
import com.example.kiosk.subscription.subscription.dto.CheckoutRequest;
import com.example.kiosk.subscription.subscription.response.SubscriptionResponse;
import com.example.kiosk.tenant.Tenant;
import com.example.kiosk.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImplementation implements CheckoutService{
    private static final Duration PROCESSING_DELAY = Duration.ofSeconds(2);
    
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    
    private final AuthServiceImplementation authService;
    private final ScheduledExecutorService paymentExecutorService;
    
    
    public List<PlanResponse> listPlans(String slug){
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Tenant not found!"));
        
        return planRepository.findByTenant(tenant).stream()
                .map(PlanResponse::from)
                .toList();
    }
    
    public SubscriptionResponse getCurrentSubscription(String userId){
        AppUser user = authService.requireUser(userId);

        return subscriptionRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(SubscriptionResponse::from)
                .orElse(null);
    }
    
    public SubscriptionResponse checkout(String userId, CheckoutRequest request){
        AppUser user = authService.requireUser(userId);
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new RuntimeException("Unknown plan !"));

        // Plan is tenant-scoped
        if(!plan.getTenant().getId().equals(user.getTenant().getId())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown Plan !");
        }

        Subscription subscription = subscriptionRepository.save(
                Subscription.builder()
                        .user(user)
                        .plan(plan)
                        .status(SubscriptionStatus.PENDING)
                        .build()
        );


        Payment payment = paymentRepository.save(
                Payment.builder()
                        .subscription(subscription)
                        .amount(plan.getPrice())
                        .currency(plan.getCurrency())
                        .status(PaymentStatus.PENDING)
                        .provider("test")
                        .externalRef(UUID.randomUUID().toString())
                        .build()
        );

        paymentExecutorService.schedule(
                () -> resolvePayment(payment.getId(), request.simulateFailure()),
                PROCESSING_DELAY.toMillis(),
                TimeUnit.MILLISECONDS
        );

        return SubscriptionResponse.from(subscription);
    }
    
    void resolvePayment(String paymentId, boolean simulateFailure){
        Payment payment = paymentRepository.findByIdWithSubscriptionAndPlan((paymentId))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown Payment !"));

        // return if the payment has already changed status
        if(payment.getStatus() == PaymentStatus.PENDING){
            return;
        }

        Subscription subscription = payment.getSubscription();
        Instant now = Instant.now();
        payment.setProcessedAt(now);

        if(simulateFailure){
            payment.setStatus(PaymentStatus.FAILED);
            subscription.setStatus(SubscriptionStatus.FAILED);
        } else {
          payment.setStatus(PaymentStatus.SUCCESS);
          subscription.setStatus(SubscriptionStatus.ACTIVE);
          subscription.setStartedAt(now);
          subscription.setExpiresAt(now.plus(Duration.ofDays(subscription.getPlan().getBillingPeriod())));
        }

        paymentRepository.save(payment);
        subscriptionRepository.save(subscription);
    }
}
