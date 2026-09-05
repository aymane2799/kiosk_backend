package com.example.kiosk.subscription.payment;

import com.example.kiosk.common.Auditable;
import com.example.kiosk.subscription.subscription.Subscription;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name= "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends Auditable {

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "external_ref")
    private String externalRef;

    @Column(name = "processed_at")
    private Instant processedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

}
