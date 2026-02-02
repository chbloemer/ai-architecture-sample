package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the order review page in checkout.
 *
 * <p>This controller handles the review step of checkout where the customer
 * reviews all order details before confirming the purchase.
 *
 * <p>The active checkout session is identified via JWT identity, removing the need
 * for session IDs in URLs.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class ReviewPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final IdentityProvider identityProvider;

  public ReviewPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final IdentityProvider identityProvider) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the order review page with all order details.
   *
   * <p>This endpoint retrieves the active checkout session for the current user
   * (via JWT identity) and displays all collected information including buyer info,
   * delivery details, payment method, and order summary for final review before
   * purchase confirmation.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the review.pug template or redirect on error
   */
  @GetMapping("/review")
  public String showReviewPage(
      final Model model,
      final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final CustomerId customerId = CustomerId.of(identity.userId().value());

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(
            GetActiveCheckoutSessionQuery.of(customerId.value()));

    if (!activeSession.found()) {
      redirectAttributes.addFlashAttribute("error", "No active checkout session found");
      return "redirect:/cart";
    }

    // Get full session details
    final GetCheckoutSessionResult session =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.of(activeSession.sessionId()));

    if (!session.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/cart";
    }

    model.addAttribute("session", session);
    model.addAttribute("title", "Review Order - Checkout");

    return "checkout/review";
  }
}
