package com.example.kiosk.auth.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserServiceImplementation implements AppUserService {

    private final AppUserRepository appUserRepository;

    public List<AppUser> listAll(String tenantId) {
        return appUserRepository.findByTenantId(tenantId);
    }
}
