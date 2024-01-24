package com.startstepszalando.ecommerceshop.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.secret-key}")
    private String secretKey;

    private final int jwtExpirationMs;

    private final String jwtCookie;

    public JwtService(@Value("${app.jwt.cookie}") String jwtCookie,
                      @Value("${app.jwt.expiration-ms}") int jwtExpirationMs,
                      @Value("${app.secret-key}") String secretKey) {
        this.jwtCookie = jwtCookie;
        this.jwtExpirationMs = jwtExpirationMs;
        this.secretKey = secretKey;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        String jwt = generateToken(extraClaims, userDetails);
        return ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge(10 * 60).httpOnly(true).build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null).path("/api").build();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
