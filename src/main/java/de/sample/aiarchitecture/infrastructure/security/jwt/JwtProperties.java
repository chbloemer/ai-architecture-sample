package de.sample.aiarchitecture.infrastructure.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT authentication.
 *
 * <p>These properties are bound from the application.yml under the
 * {@code app.security.jwt} prefix.
 *
 * <p><b>Example configuration:</b>
 * <pre>
 * app:
 *   security:
 *     jwt:
 *       secret: ${JWT_SECRET:dev-only-secret-key}
 *       anonymous-expiration-days: 30
 *       registered-expiration-days: 7
 *       issuer: ai-architecture-sample
 *       cookie-name: shop-identity
 * </pre>
 *
 * @param secret the secret key for signing JWTs (must be at least 256 bits for HS256)
 * @param anonymousExpirationDays how long anonymous tokens remain valid (default: 30 days)
 * @param registeredExpirationDays how long registered user tokens remain valid (default: 7 days)
 * @param issuer the token issuer claim (iss)
 * @param cookieName the name of the cookie that stores the JWT
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
    String secret,
    int anonymousExpirationDays,
    int registeredExpirationDays,
    String issuer,
    String cookieName) {

  /**
   * Default cookie name if not configured.
   */
  public static final String DEFAULT_COOKIE_NAME = "shop-identity";

  /**
   * Default issuer if not configured.
   */
  public static final String DEFAULT_ISSUER = "ai-architecture-sample";

  public JwtProperties {
    if (secret == null || secret.length() < 32) {
      throw new IllegalArgumentException(
          "JWT secret must be at least 32 characters (256 bits) for HS256");
    }
    if (anonymousExpirationDays <= 0) {
      anonymousExpirationDays = 30;
    }
    if (registeredExpirationDays <= 0) {
      registeredExpirationDays = 7;
    }
    if (issuer == null || issuer.isBlank()) {
      issuer = DEFAULT_ISSUER;
    }
    if (cookieName == null || cookieName.isBlank()) {
      cookieName = DEFAULT_COOKIE_NAME;
    }
  }

  /**
   * Returns the anonymous token expiration in milliseconds.
   *
   * @return expiration time in milliseconds
   */
  public long anonymousExpirationMs() {
    return (long) anonymousExpirationDays * 24 * 60 * 60 * 1000;
  }

  /**
   * Returns the registered token expiration in milliseconds.
   *
   * @return expiration time in milliseconds
   */
  public long registeredExpirationMs() {
    return (long) registeredExpirationDays * 24 * 60 * 60 * 1000;
  }
}
