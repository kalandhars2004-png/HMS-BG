package com.phegondev.InventoryManagementSystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtils {

    /** HMAC-SHA256 requires at least a 256-bit (32-byte) key. */
    private static final int MIN_SECRET_BYTES = 32;

    private SecretKey key;

    @Value("${secreteJwtString}")
    private String secreteJwtString ;

    /**
     * Token lifetime in minutes. Defaults to 12 hours — a working shift. The previous
     * value was a hardcoded ~21 days (the constant read 100L where 1000L was intended,
     * so it never matched its own "6 months" comment). Long-lived tokens are especially
     * bad here because there is no refresh and no revocation: a leaked token is valid
     * until it expires.
     */
    @Value("${jwt.expiration-minutes:720}")
    private long expirationMinutes;

    @PostConstruct
    private void init(){
        if (secreteJwtString == null || secreteJwtString.isBlank()) {
            throw new IllegalStateException(
                    "JWT signing secret is not configured. Set the JWT_SECRET environment variable.");
        }

        byte[] keyByte = secreteJwtString.getBytes(StandardCharsets.UTF_8);

        if (keyByte.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT signing secret must be at least " + MIN_SECRET_BYTES
                            + " bytes for HMAC-SHA256 (got " + keyByte.length + ").");
        }

        this.key = new SecretKeySpec(keyByte, "HmacSHA256");
        log.info("JWT signing key initialised; tokens expire after {} minutes", expirationMinutes);
    }

    private long expirationMillis(){
        return expirationMinutes * 60L * 1000L;
    }

    /** Human-readable lifetime, returned to the client on login. */
    public String getExpirationDescription(){
        return expirationMinutes + " minutes";
    }

    public String generateToken(String email){
        return generateToken(email, null, null);
    }

    public String generateToken(String email, Long branchId, String role){
        var builder = Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis()));
        if (branchId != null) builder.claim("branchId", branchId);
        if (role != null) builder.claim("role", role);
        return builder.signWith(key).compact();
    }

    public String getUsernameFromToken(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public Long getBranchIdFromToken(String token){
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Object v = claims.get("branchId");
            if (v instanceof Number n) return n.longValue();
            if (v instanceof String s) return Long.valueOf(s);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T extractClaims(String token, Function<Claims,T> claimsTFunction){
        return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
    }
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }








}
