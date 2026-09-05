package com.example.kiosk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class PaymentExecutorConfig {
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService paymentExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }
}
