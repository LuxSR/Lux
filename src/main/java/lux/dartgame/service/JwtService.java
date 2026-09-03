package lux.dartgame.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lux.dartgame.config.JwtProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public final class JwtService {
    private static final int MINUTE_LENGTH = 60;

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(final JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder()
                .decode(properties.secret()));
        this.expirationMinutes = properties.expirationMinutes();
    }

    public String generateToken(final UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * MINUTE_LENGTH);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(final String token, final UserDetails userDetails) {
        try {
            var claims = parseClaims(token);
            return claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
