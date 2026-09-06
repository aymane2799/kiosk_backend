package com.example.kiosk.auth.jwt;

import com.example.kiosk.admin.Admin;
import com.example.kiosk.auth.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;

public interface JwtService {
    String generateToken(AppUser user);
    String generateAdminToken(Admin admin);
    Jws<Claims> parseToken(String token);

    private byte[] sha256(String value) {
       return null;
    }
    private JwtBuilder baseToken(String subject) {
       return null;
    }
}
