package com.example.kiosk.auth.jwt;

import com.example.kiosk.auth.user.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public interface JwtService {
    String generateToken(AppUser user);
    Jws<Claims> parseToken(String token);
    private byte[] sha256(String value) {
       return null;
    }
}
