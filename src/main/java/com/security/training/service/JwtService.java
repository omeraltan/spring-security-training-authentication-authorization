package com.security.training.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    public String extractUsername(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }

    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        try {
            final String username = extractUsername(jwtToken);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken);

            if (!isValid) {
                log.warn("Token validation failed for user: {}", username);
            }
            return isValid;

        }catch (Exception e){
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return  claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
            .parser()
            .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(UserDetails userDetails) {
        return generateJWT(new HashMap<>(), userDetails, jwtExpiration);
    }

    private String generateJWT(Map<String, Object> extractClaims, UserDetails userDetails, long expiration) {
        return buildToken(extractClaims, userDetails, expiration);
    }

    private String buildToken(Map<String, Object> extractClaims, UserDetails userDetails, long expiration) {
        var authorities = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return Jwts.builder()
            .claims(extractClaims)
            .subject(userDetails.getUsername())
            .claim("authorities", authorities)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateJWT(new HashMap<>(), userDetails, refreshExpiration);
    }
}
