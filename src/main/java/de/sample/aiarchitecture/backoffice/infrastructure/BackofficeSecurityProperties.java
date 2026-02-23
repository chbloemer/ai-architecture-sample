package de.sample.aiarchitecture.backoffice.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for backoffice admin authentication.
 *
 * <p>Configures the in-memory admin user for the backoffice. In production, replace with
 * OAuth2/Keycloak by swapping the {@link BackofficeSecurityConfiguration}.
 *
 * @param username the admin username (default: "admin")
 * @param password the admin password (default: "admin")
 */
@ConfigurationProperties(prefix = "app.security.backoffice")
public record BackofficeSecurityProperties(String username, String password) {

  public BackofficeSecurityProperties {
    if (username == null || username.isBlank()) {
      username = "admin";
    }
    if (password == null || password.isBlank()) {
      password = "admin";
    }
  }
}
