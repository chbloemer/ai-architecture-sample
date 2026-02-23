package de.sample.aiarchitecture.backoffice.adapter.incoming.web;

import de.sample.aiarchitecture.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import de.sample.aiarchitecture.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import de.sample.aiarchitecture.backoffice.application.geteventpublications.GetEventPublicationsResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for the backoffice pages.
 *
 * <p>Handles the backoffice login page and the event publication log page. The login page is served
 * separately from the storefront login (which uses JWT) — the backoffice uses form-based session
 * authentication.
 *
 * <p><b>ViewModel Pattern:</b> Use Case -> Result -> Controller -> ViewModel -> Template
 */
@Controller
@RequestMapping("/backoffice")
public class EventPublicationPageController {

  private final GetEventPublicationsInputPort getEventPublicationsInputPort;

  public EventPublicationPageController(
      final GetEventPublicationsInputPort getEventPublicationsInputPort) {
    this.getEventPublicationsInputPort = getEventPublicationsInputPort;
  }

  /**
   * Displays the backoffice login page.
   *
   * @param error whether the previous login attempt failed
   * @param logout whether the user just logged out
   * @param model the Spring MVC model
   * @param request the HTTP request (for CSRF token)
   * @return the Pug template name
   */
  @GetMapping("/login")
  public String showLoginPage(
      @RequestParam(required = false) final String error,
      @RequestParam(required = false) final String logout,
      final Model model,
      final HttpServletRequest request) {
    final CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrfToken != null) {
      model.addAttribute("_csrf", csrfToken);
    }
    model.addAttribute("title", "Backoffice Login");

    return "backoffice/login";
  }

  /**
   * Displays the event publication log page.
   *
   * @param model the Spring MVC model
   * @return the Pug template name
   */
  @GetMapping("/events")
  public String showEventPublicationLog(final Model model) {
    final GetEventPublicationsResult result =
        getEventPublicationsInputPort.execute(new GetEventPublicationsQuery());

    final EventPublicationPageViewModel viewModel = EventPublicationPageViewModel.from(result);

    model.addAttribute("eventLog", viewModel);
    model.addAttribute("title", "Event Publication Log");

    return "backoffice/events";
  }
}
