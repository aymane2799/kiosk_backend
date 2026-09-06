package com.example.kiosk.auth.user;

import java.util.List;

public interface AppUserService {
    List<AppUser> listAll(String tenantId);
}
