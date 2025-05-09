package com.eidiko.user_service.util;
import com.eidiko.user_service.exception.InvalidJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY ;
    @Value("${jwt.access-token.expiration}")
    private  long ACCESS_TOKEN_EXPIRATION ;
    @Value("${jwt.refresh-token.expiration}")
    private  long REFRESH_TOKEN_EXPIRATION ;

    public String generateAccessToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSignInKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSignInKey())
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


    public boolean validateToken(String token)  {
        boolean result = false;
        try {
            log.info("Validating token {} " ,token);
            Claims claims = extractAllClaims(token);
           log.info("Claims {}" , claims);
            result = true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Token expired {}" , e.getMessage());
        } catch (JwtException e) {
            log.error("JWT error {} " , e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument {}" , e.getMessage());
        }
        return result;
    }

    public Instant getExpirationDateFromToken(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException | SignatureException | ExpiredJwtException | IllegalArgumentException e) {
            log.error("JWT error: {}", e.getMessage());
            throw new InvalidJwtException("Invalid JWT token", e);
        }
    }


}
