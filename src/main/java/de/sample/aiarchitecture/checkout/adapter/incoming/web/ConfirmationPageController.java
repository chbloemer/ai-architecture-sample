package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutInputPort;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.application.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionResult;
import de.sample.aiarchitecture.checkout.domain.model.CustomerId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for order confirmation in checkout.
 *
 * <p>This controller handles the confirmation step of checkout where the customer
 * confirms their order and views the thank you page.
 *
 * <p>The checkout session is identified via JWT identity, removing the need
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
public class ConfirmationPageController {

  private final ConfirmCheckoutInputPort confirmCheckoutInputPort;
  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final GetConfirmedCheckoutSessionInputPort getConfirmedCheckoutSessionInputPort;
  private final IdentityProvider identityProvider;

  public ConfirmationPageController(
      final ConfirmCheckoutInputPort confirmCheckoutInputPort,
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final GetConfirmedCheckoutSessionInputPort getConfirmedCheckoutSessionInputPort,
      final IdentityProvider identityProvider) {
    this.confirmCheckoutInputPort = confirmCheckoutInputPort;
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.getConfirmedCheckoutSessionInputPort = getConfirmedCheckoutSessionInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Confirms the checkout and places the order.
   *
   * <p>This endpoint finds the active checkout session for the current user
   * (via JWT identity), processes the order confirmation, and redirects
   * to the confirmation page on success.
   *
   * @param redirectAttributes for passing flash messages
   * @return redirect to confirmation page or back to review on error
   */
  @PostMapping("/confirm")
  public String confirmOrder(final RedirectAttributes redirectAttributes) {

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

    try {
      confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(activeSession.sessionId()));

      redirectAttributes.addFlashAttribute("orderConfirmed", true);
      return "redirect:/checkout/confirmation";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/review";
    }
  }

  /**
   * Displays the order confirmation (thank you) page.
   *
   * <p>This endpoint shows the confirmation page after a successful order.
   * It looks up the confirmed/completed session for the current user
   * (via JWT identity). It is only accessible after the checkout has been confirmed.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the confirmation.pug template or redirect on error
   */
  @GetMapping("/confirmation")
  public String showConfirmationPage(
      final Model model,
      final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final CustomerId customerId = CustomerId.of(identity.userId().value());

    // Find confirmed or completed checkout session for the user
    final GetConfirmedCheckoutSessionResult confirmedSession =
        getConfirmedCheckoutSessionInputPort.execute(
            GetConfirmedCheckoutSessionQuery.of(customerId.value()));

    if (!confirmedSession.found()) {
      redirectAttributes.addFlashAttribute("error", "No confirmed order found");
      return "redirect:/cart";
    }

    // Get full session details
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.of(confirmedSession.sessionId()));

    if (!result.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/cart";
    }

    // Only allow access to confirmation page if the session is confirmed or completed
    final var snapshot = result.session();
    if (!snapshot.isConfirmed() && !snapshot.isCompleted()) {
      redirectAttributes.addFlashAttribute("error", "Order has not been confirmed yet");
      return "redirect:/checkout/review";
    }

    // Convert to page-specific ViewModel
    final ConfirmationPageViewModel viewModel = ConfirmationPageViewModel.fromSnapshot(snapshot);

    model.addAttribute("orderConfirmation", viewModel);
    model.addAttribute("title", "Order Confirmed - Thank You!");

    return "checkout/confirmation";
  }
}
