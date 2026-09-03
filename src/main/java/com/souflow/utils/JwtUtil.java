package com.souflow.utils;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey; 

import org.springframework.beans.factory.annotation.Value;

@Component
public class JwtUtil {

    @Value("${jwt.secret:blackfloydcantbreathebecausea12kneesonhisneckforusingacounterfeitmoneytobuyabanana}")
    private String secret;

    @Value("${jwt.expiration:1800000}")
    private long expiration;
    
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(String username, String roleCode) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roleCode", roleCode)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return extractAllClaims(token).get("roleCode", String.class);
    }

    public boolean isValid(String token) {
        try {
            extractAllClaims(token); // will throw if invalid/expired
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}