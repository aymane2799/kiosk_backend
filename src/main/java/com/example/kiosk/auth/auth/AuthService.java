package com.example.kiosk.auth.auth;

import com.example.kiosk.auth.auth.dto.LoginRequest;
import com.example.kiosk.auth.auth.response.LoginResponse;
import com.example.kiosk.auth.jwt.JwtService;
import com.example.kiosk.auth.user.AppUser;
import com.example.kiosk.auth.user.AppUserRepository;
import com.example.kiosk.auth.user.Role;
import com.example.kiosk.tenant.Tenant;
import com.example.kiosk.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public LoginResponse login(LoginRequest loginRequest) {
        Tenant tenant = tenantRepository.findBySlug(loginRequest.tenantSlug())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant"));


        PartnerIdentity identity =  decodePartnerIdentity(loginRequest.partnerToken());

        if(!tenant.getProviderId().equals(identity.providerId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid provider token for this tenant");
        }

        AppUser user = appUserRepository.findByTenantAndExternalId(tenant, identity.externalId())
                .orElseGet(() -> appUserRepository.save(
                        AppUser.builder()
                                .tenant(tenant)
                                .externalId(identity.externalId())
                                .email(identity.email())
                                .firstName(identity.firstName())
                                .lastName(identity.lastName())
                                .role(Role.USER)
                                .build()
                ));

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, user.getId(), tenant.getId(), user.getRole());
    }


    private PartnerIdentity decodePartnerIdentity(String partnerToken) {
        try {
            byte[] decoded = Base64.getDecoder().decode(partnerToken);

            return objectMapper.readValue(decoded, PartnerIdentity.class);
        }catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid partner token");
        }
    }
}
