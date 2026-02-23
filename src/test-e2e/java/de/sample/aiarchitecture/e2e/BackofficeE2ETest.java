package de.sample.aiarchitecture.e2e;

import static org.junit.jupiter.api.Assertions.*;

import de.sample.aiarchitecture.e2e.pages.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the backoffice module.
 *
 * <p>Tests the complete backoffice experience including:
 *
 * <ul>
 *   <li>Backoffice login and authentication
 *   <li>Event publication log display and summary
 *   <li>Login error handling for invalid credentials
 *   <li>Logout flow and redirect behaviour
 *   <li>Unauthenticated access protection
 *   <li>Event payload inspection
 * </ul>
 *
 * <p>Prerequisites:
 *
 * <ul>
 *   <li>Application running at configured base URL
 *   <li>Backoffice admin credentials: username "admin", password "admin"
 *   <li>At least one domain event published (ProductCreated events are published on startup)
 * </ul>
 */
@DisplayName("Backoffice E2E Tests")
class BackofficeE2ETest extends BaseE2ETest {

  private static final String ADMIN_USERNAME = "admin";
  private static final String ADMIN_PASSWORD = "admin";

  @Test
  @DisplayName("Login as admin and view the event log")
  void loginAndViewEventLog() {
    BackofficeLoginPage loginPage = BackofficeLoginPage.navigateTo(page);
    BackofficeEventsPage eventsPage =
        loginPage.fillCredentials(ADMIN_USERNAME, ADMIN_PASSWORD).submit();

    assertTrue(eventsPage.isOnPage(), "Should be on the event log page after login");
    assertTrue(eventsPage.hasSummary(), "Event log should display the summary section");
    assertTrue(
        eventsPage.getTotalEvents() >= 0, "Total events count should be a non-negative number");
  }

  @Test
  @DisplayName("Event log shows published domain events with correct counts")
  void eventLogShowsPublishedEvents() {
    BackofficeEventsPage eventsPage =
        BackofficeLoginPage.navigateTo(page)
            .fillCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)
            .submit();

    int total = eventsPage.getTotalEvents();
    assertTrue(total > 0, "Event log should contain events published during application startup");
    assertTrue(eventsPage.getEventCount() > 0, "Event list should render at least one event item");

    String firstEventType = eventsPage.getFirstEventType();
    assertFalse(firstEventType.isBlank(), "First event type label should not be empty");

    int completed = eventsPage.getCompletedCount();
    int incomplete = eventsPage.getIncompleteCount();
    assertEquals(
        total, completed + incomplete, "Completed and incomplete counts should add up to total");
  }

  @Test
  @DisplayName("Invalid credentials show a login error message")
  void invalidLoginShowsError() {
    BackofficeLoginPage loginPage = BackofficeLoginPage.navigateTo(page);
    loginPage.fillCredentials(ADMIN_USERNAME, "wrongpassword").submitExpectingError();

    assertTrue(
        loginPage.hasLoginError(),
        "Login page should display an error message after failed authentication");
  }

  @Test
  @DisplayName("Logout redirects to login page and shows logout confirmation")
  void logoutRedirectsToLoginPage() {
    BackofficeEventsPage eventsPage =
        BackofficeLoginPage.navigateTo(page)
            .fillCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)
            .submit();

    BackofficeLoginPage loginPage = eventsPage.clickLogout();

    assertTrue(
        loginPage.hasLogoutMessage(),
        "Login page should display a logout confirmation message after logout");
  }

  @Test
  @DisplayName("Unauthenticated access to events page redirects to login")
  void unauthenticatedAccessRedirectsToLogin() {
    page.navigate(BASE_URL + "/backoffice/events");

    // Spring Security redirects to /backoffice/login when unauthenticated
    BackofficeLoginPage loginPage = new BackofficeLoginPage(page);

    assertTrue(
        page.url().contains("/backoffice/login"),
        "Unauthenticated request to events page should redirect to the backoffice login page");
  }

  @Test
  @DisplayName("Event payload can be expanded to reveal details")
  void eventPayloadCanBeExpanded() {
    BackofficeEventsPage eventsPage =
        BackofficeLoginPage.navigateTo(page)
            .fillCredentials(ADMIN_USERNAME, ADMIN_PASSWORD)
            .submit();

    assertTrue(
        eventsPage.getEventCount() > 0, "At least one event must exist before expanding a payload");

    eventsPage.expandFirstEventPayload();

    String payload = eventsPage.getFirstEventPayloadText();
    assertFalse(payload.isBlank(), "Expanded event payload should contain non-empty content");
  }
}
