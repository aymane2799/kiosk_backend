package com.example.kiosk.subscription.payment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    // JPL QUERY
    // @Query("select p from Payment p join fetch p.subscription s join s.plan where p.id = :id")
    // Optional<Payment> findByIdWithSubscriptionAndPlan(@Param("id") String id);

    @EntityGraph(attributePaths = {"subscription", "plan"})
    Optional<Payment> findByIdWithSubscriptionAndPlan(String id);
}
