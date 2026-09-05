package com.example.kiosk.entitlement;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.Content;
import com.example.kiosk.subscription.subscription.Subscription;

public interface EntitlementService {
    boolean hasAccess(AppUser user, Content content);
}
