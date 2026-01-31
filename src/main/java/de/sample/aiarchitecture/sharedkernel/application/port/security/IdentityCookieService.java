package de.sample.aiarchitecture.sharedkernel.application.port.security;

import de.sample.aiarchitecture.sharedkernel.application.port.OutputPort;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Output port for managing identity cookies.
 *
 * <p>This port allows adapters to set and clear authentication cookies without
 * depending on infrastructure implementations. It provides a clean abstraction
 * for cookie management in the authentication flow.
 *
 * @see OutputPort
 */
public interface IdentityCookieService extends OutputPort {

  /**
   * Sets an identity cookie for a registered user.
   *
   * @param response the HTTP response to set the cookie on
   * @param token the registered user's JWT token
   */
  void setRegisteredUserCookie(HttpServletResponse response, String token);

  /**
   * Clears the identity cookie (for logout).
   *
   * @param response the HTTP response
   */
  void clearIdentityCookie(HttpServletResponse response);
}
