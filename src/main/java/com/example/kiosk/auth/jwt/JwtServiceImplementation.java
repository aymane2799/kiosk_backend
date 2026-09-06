package com.example.kiosk.auth.jwt;

import com.example.kiosk.admin.Admin;
import com.example.kiosk.auth.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.time.Instant;

@Component
public class JwtServiceImplementation implements JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtServiceImplementation(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(sha256(secretKey));
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser user) {

        return baseToken(user.getId())
                .claim("tenantId", user.getTenant().getId())
                .claim("role", user.getRole())
                .compact();
    }

    public String generateAdminToken(Admin admin) {

        return baseToken(admin.getId())
                .claim("role", admin.getRole())
                .compact();
    }

    private JwtBuilder baseToken(String subject) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey);
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes());
        }catch (NoSuchAlgorithmException e){
            throw new IllegalStateException(e);
        }
    }
}
