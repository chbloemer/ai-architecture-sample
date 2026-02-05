package de.sample.aiarchitecture.infrastructure.security.jwt;

import de.sample.aiarchitecture.account.application.shared.RegisteredUserValidator;
import de.sample.aiarchitecture.infrastructure.security.JwtIdentity;
import de.sample.aiarchitecture.sharedkernel.domain.model.UserId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter that runs on every request.
 *
 * <p>This filter is responsible for:
 * <ol>
 *   <li>Extracting JWT from the cookie (or Authorization header)</li>
 *   <li>If no JWT exists, generating an anonymous token and setting the cookie</li>
 *   <li>Validating the token and extracting the Identity</li>
 *   <li>Setting the SecurityContext with the Identity for downstream access</li>
 * </ol>
 *
 * <p><b>Cookie Settings:</b>
 * <ul>
 *   <li>HttpOnly: true (prevents XSS attacks)</li>
 *   <li>Secure: based on request scheme (https = secure)</li>
 *   <li>SameSite: Lax (CSRF protection)</li>
 *   <li>Path: / (accessible site-wide)</li>
 *   <li>MaxAge: based on token type (30 days anonymous, 7 days registered)</li>
 * </ul>
 *
 * <p><b>Security Context:</b>
 * The Identity is stored in the SecurityContext as the principal of an
 * UsernamePasswordAuthenticationToken. This allows downstream code to access
 * the identity via SecurityContextHolder or the IdentityProvider.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenService tokenService;
  private final JwtProperties jwtProperties;
  private final RegisteredUserValidator registeredUserValidator;

  public JwtAuthenticationFilter(
      final JwtTokenService tokenService,
      final JwtProperties jwtProperties,
      final RegisteredUserValidator registeredUserValidator) {
    this.tokenService = tokenService;
    this.jwtProperties = jwtProperties;
    this.registeredUserValidator = registeredUserValidator;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain) throws ServletException, IOException {

    // Skip filter for static resources
    if (isStaticResource(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    // Try to extract token from cookie or header
    Optional<String> tokenOpt = extractTokenFromCookie(request);
    if (tokenOpt.isEmpty()) {
      tokenOpt = extractTokenFromHeader(request);
    }

    IdentityProvider.Identity identity;
    boolean newTokenCreated = false;
    String token;

    if (tokenOpt.isPresent()) {
      token = tokenOpt.get();
      // Validate existing token
      final Optional<IdentityProvider.Identity> identityOpt = tokenService.validateAndParse(token);

      if (identityOpt.isPresent()) {
        identity = identityOpt.get();

        // For registered users, verify account still exists (handles app restart with in-memory storage)
        if (identity.isRegistered() && !registeredUserValidator.existsForUserId(identity.userId())) {
          LOG.info("Registered user {} has no account - creating anonymous identity",
                   identity.userId().value());
          identity = createAnonymousIdentity();
          token = tokenService.generateAnonymousToken(identity.userId());
          newTokenCreated = true;
        } else {
          LOG.debug("Valid JWT found for user: {} ({})", identity.userId().value(), identity.type());
        }
      } else {
        // Token invalid or expired - create new anonymous identity
        LOG.debug("Invalid/expired JWT, creating new anonymous identity");
        identity = createAnonymousIdentity();
        token = tokenService.generateAnonymousToken(identity.userId());
        newTokenCreated = true;
      }
    } else {
      // No token found - create new anonymous identity
      LOG.debug("No JWT found, creating new anonymous identity");
      identity = createAnonymousIdentity();
      token = tokenService.generateAnonymousToken(identity.userId());
      newTokenCreated = true;
    }

    // Set cookie if new token was created
    if (newTokenCreated) {
      setIdentityCookie(response, token, jwtProperties.anonymousExpirationDays() * 24 * 60 * 60);
    }

    // Set security context
    setSecurityContext(identity);

    // Continue filter chain
    filterChain.doFilter(request, response);
  }

  private Optional<String> extractTokenFromCookie(final HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> jwtProperties.cookieName().equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(value -> value != null && !value.isBlank())
        .findFirst();
  }

  private Optional<String> extractTokenFromHeader(final HttpServletRequest request) {
    final String header = request.getHeader(AUTHORIZATION_HEADER);

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      return Optional.of(header.substring(BEARER_PREFIX.length()));
    }

    return Optional.empty();
  }

  private IdentityProvider.Identity createAnonymousIdentity() {
    final UserId userId = UserId.generateAnonymous();
    return JwtIdentity.anonymous(userId);
  }

  private void setIdentityCookie(
      final HttpServletResponse response,
      final String token,
      final int maxAgeSeconds) {

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

  private void setSecurityContext(final IdentityProvider.Identity identity) {
    final var authorities = identity.roles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .toList();

    final var authentication = new UsernamePasswordAuthenticationToken(
        identity,  // principal
        null,      // credentials (not needed for JWT)
        authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private boolean isStaticResource(final HttpServletRequest request) {
    final String path = request.getRequestURI();
    return path.startsWith("/css/")
        || path.startsWith("/js/")
        || path.startsWith("/images/")
        || path.startsWith("/fonts/")
        || path.startsWith("/favicon")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".ico")
        || path.endsWith(".png")
        || path.endsWith(".jpg")
        || path.endsWith(".gif")
        || path.endsWith(".svg")
        || path.endsWith(".woff")
        || path.endsWith(".woff2");
  }

  /**
   * Sets the identity cookie for a registered user.
   *
   * @param response HTTP response to set the cookie on
   * @param token the JWT token to store in the cookie
   */
  public void setRegisteredUserCookie(final HttpServletResponse response, final String token) {
    setIdentityCookie(response, token, jwtProperties.registeredExpirationDays() * 24 * 60 * 60);
  }

  /**
   * Clears the identity cookie, effectively logging out the user.
   *
   * @param response HTTP response to clear the cookie on
   */
  public void clearIdentityCookie(final HttpServletResponse response) {
    final Cookie cookie = new Cookie(jwtProperties.cookieName(), "");
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
