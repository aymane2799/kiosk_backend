package com.example.kiosk.auth.jwt;

import com.example.kiosk.common.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Claims claims = jwtService.parseToken(token).getPayload();

                String userId = claims.getSubject();
                String tenantId = claims.get("tenantId", String.class);
                String role = claims.get("role", String.class);

                // in case it's an admin (admin tokens don't have tenantId as a claim)
                if(tenantId != null) TenantContext.setTenantId(tenantId);

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        )
                );

            }catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }

        }

        try {
            filterChain.doFilter(request, response);
        }
        finally {
            TenantContext.clear();
        }
    }
}
