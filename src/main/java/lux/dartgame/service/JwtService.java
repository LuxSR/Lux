package lux.dartgame.service;

import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lux.dartgame.config.JwtProperties;
import lux.dartgame.constants.Constants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public final class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(final JwtProperties properties) {
        // REVIEW(bug): if JWT_SECRET is not valid base64, or decodes to fewer than 32 bytes, this throws inside a constructor and the container crash-loops with an error that does not mention the secret. Validate it in JwtProperties (a @Size on the decoded length) and document the expected format.
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder()
                .decode(properties.secret()));
        this.expirationMinutes = properties.expirationMinutes();
    }

    // REVIEW(sec): the token carries only the subject. That is a deliberate, defensible choice (it means a revoked role takes effect immediately), but it is why every request costs a user lookup. Just know which trade you picked.
    public String generateToken(final UserDetails userDetails) {
        // REVIEW(noob): logging a username at INFO on every token generation. Fine here, but get into the habit of asking whether a log line would be acceptable once this is real user data in a cloud log sink.
        log.info("Generating token for user: {}", userDetails.getUsername());
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * Constants.SECONDS_PER_MINUTE);

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
            boolean valid = claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(new Date());
            log.debug("Token validation for user {}: {}", userDetails.getUsername(), valid);
            return valid;
        } catch (JwtException e) {
            log.debug("Token validation failed for user {}: {}", userDetails.getUsername(),
                    e.getMessage());
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
