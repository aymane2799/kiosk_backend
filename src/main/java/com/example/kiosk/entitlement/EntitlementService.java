package com.example.kiosk.entitlement;

import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.content.Content;

public interface EntitlementService {
    boolean hasAccess(AppUser user, Content content);
    boolean hasActivePremium(AppUser user);
}
