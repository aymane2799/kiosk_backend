package com.example.kiosk.subscription.subscription;

import com.example.kiosk.common.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
}
