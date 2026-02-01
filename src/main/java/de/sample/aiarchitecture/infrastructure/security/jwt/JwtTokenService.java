package de.sample.aiarchitecture.infrastructure.security.jwt;

import de.sample.aiarchitecture.infrastructure.security.JwtIdentity;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import de.sample.aiarchitecture.account.application.shared.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * JWT implementation of the TokenService.
 *
 * <p>This service handles all JWT token operations including:
 * <ul>
 *   <li>Generating anonymous tokens for first-time visitors</li>
 *   <li>Generating registered tokens after login/registration</li>
 *   <li>Validating and parsing tokens from cookies</li>
 * </ul>
 *
 * <p><b>Token Structure:</b>
 * <pre>
 * Header: { "alg": "HS256", "typ": "JWT" }
 * Payload: {
 *   "sub": "user-id-here",
 *   "type": "anonymous" | "registered",
 *   "email": "user@example.com",  // only for registered
 *   "roles": ["CUSTOMER"],        // only for registered
 *   "iss": "ai-architecture-sample",
 *   "iat": 1706688000,
 *   "exp": 1707292800
 * }
 * </pre>
 */
@Service
public class JwtTokenService implements TokenService {

  private static final Logger LOG = LoggerFactory.getLogger(JwtTokenService.class);

  private static final String CLAIM_TYPE = "type";
  private static final String CLAIM_EMAIL = "email";
  private static final String CLAIM_ROLES = "roles";

  private static final String TYPE_ANONYMOUS = "anonymous";
  private static final String TYPE_REGISTERED = "registered";

  private final JwtProperties properties;
  private final SecretKey secretKey;

  public JwtTokenService(final JwtProperties properties) {
    this.properties = properties;
    this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generates an anonymous JWT token for first-time visitors.
   *
   * @param userId the user's unique identifier
   * @return the generated JWT token string
   */
  @NonNull
  public String generateAnonymousToken(@NonNull final UserId userId) {
    final Date now = new Date();
    final Date expiration = new Date(now.getTime() + properties.anonymousExpirationMs());

    return Jwts.builder()
        .subject(userId.value())
        .claim(CLAIM_TYPE, TYPE_ANONYMOUS)
        .issuer(properties.issuer())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Generates a JWT token for registered users.
   *
   * @param userId the user's unique identifier
   * @param email the user's email address
   * @param roles the user's roles
   * @return the generated JWT token string
   */
  @Override
  @NonNull
  public String generateRegisteredToken(
      @NonNull final UserId userId,
      @NonNull final String email,
      @NonNull final Set<String> roles) {
    final Date now = new Date();
    final Date expiration = new Date(now.getTime() + properties.registeredExpirationMs());

    return Jwts.builder()
        .subject(userId.value())
        .claim(CLAIM_TYPE, TYPE_REGISTERED)
        .claim(CLAIM_EMAIL, email)
        .claim(CLAIM_ROLES, roles.stream().toList())
        .issuer(properties.issuer())
        .issuedAt(now)
        .expiration(expiration)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Validates and parses a JWT token, returning the Identity if valid.
   *
   * @param token the JWT token to validate
   * @return an Optional containing the Identity if valid, empty otherwise
   */
  @NonNull
  public Optional<IdentityProvider.Identity> validateAndParse(@NonNull final String token) {
    try {
      final Claims claims = Jwts.parser()
          .verifyWith(secretKey)
          .requireIssuer(properties.issuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();

      return Optional.of(buildIdentityFromClaims(claims));

    } catch (ExpiredJwtException e) {
      LOG.debug("JWT token expired: {}", e.getMessage());
      return Optional.empty();
    } catch (JwtException e) {
      LOG.warn("Invalid JWT token: {}", e.getMessage());
      return Optional.empty();
    } catch (IllegalArgumentException e) {
      LOG.warn("Error parsing JWT claims: {}", e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Checks if a JWT token is expired.
   *
   * @param token the JWT token to check
   * @return true if the token is expired or invalid, false otherwise
   */
  public boolean isExpired(@NonNull final String token) {
    try {
      Jwts.parser()
          .verifyWith(secretKey)
          .build()
          .parseSignedClaims(token);
      return false;
    } catch (ExpiredJwtException e) {
      return true;
    } catch (JwtException e) {
      // Invalid token, treat as expired
      return true;
    }
  }

  private IdentityProvider.Identity buildIdentityFromClaims(final Claims claims) {
    final String subject = claims.getSubject();
    final String type = claims.get(CLAIM_TYPE, String.class);

    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("JWT subject claim is missing");
    }

    final UserId userId = UserId.of(subject);

    if (TYPE_REGISTERED.equals(type)) {
      final String email = claims.get(CLAIM_EMAIL, String.class);
      @SuppressWarnings("unchecked")
      final List<String> rolesList = claims.get(CLAIM_ROLES, List.class);
      final Set<String> roles = rolesList != null ? new HashSet<>(rolesList) : Set.of();

      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Registered JWT missing email claim");
      }

      return JwtIdentity.registered(userId, email, roles);
    }

    // Default to anonymous if type is missing or "anonymous"
    return JwtIdentity.anonymous(userId);
  }
}
