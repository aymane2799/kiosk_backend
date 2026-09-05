package com.example.kiosk.subscription.plan;

import com.example.kiosk.common.Auditable;
import com.example.kiosk.content.ContentTier;
import com.example.kiosk.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan extends Auditable {

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 20)
    private ContentTier tier;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "billing_period", nullable = false)
    private int billingPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}
