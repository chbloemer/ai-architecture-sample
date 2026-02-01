package de.sample.aiarchitecture.infrastructure.security.jwt;

import de.sample.aiarchitecture.account.application.shared.IdentitySession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * JWT-based implementation of IdentitySession.
 *
 * <p>This component manages user session identity via HTTP cookies. It is request-scoped
 * because it needs access to the current HTTP response to set/clear cookies.
 *
 * <p><b>Cookie Settings:</b>
 * <ul>
 *   <li>HttpOnly: true (prevents XSS attacks)</li>
 *   <li>Secure: false for local development (should be true in production)</li>
 *   <li>SameSite: Lax (CSRF protection)</li>
 *   <li>Path: / (accessible site-wide)</li>
 *   <li>MaxAge: configured via JwtProperties</li>
 * </ul>
 */
@Component
@RequestScope
public class JwtIdentitySession implements IdentitySession {

  private final JwtProperties jwtProperties;
  private final HttpServletResponse response;

  public JwtIdentitySession(
      final JwtProperties jwtProperties,
      final HttpServletResponse response) {
    this.jwtProperties = jwtProperties;
    this.response = response;
  }

  @Override
  public void setRegisteredIdentity(@NonNull final String token) {
    final int maxAgeSeconds = jwtProperties.registeredExpirationDays() * 24 * 60 * 60;
    setIdentityCookie(token, maxAgeSeconds);
  }

  @Override
  public void clearIdentity() {
    final Cookie cookie = new Cookie(jwtProperties.cookieName(), "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }

  private void setIdentityCookie(final String token, final int maxAgeSeconds) {
    final Cookie cookie = new Cookie(jwtProperties.cookieName(), token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    // In production, this should be true (HTTPS only)
    // For local development, we allow non-secure cookies
    cookie.setSecure(false);
    // SameSite=Lax provides CSRF protection while allowing normal navigation
    cookie.setAttribute("SameSite", "Lax");

    response.addCookie(cookie);
  }
}
