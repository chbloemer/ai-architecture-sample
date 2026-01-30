package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutInputPort;
import de.sample.aiarchitecture.checkout.application.confirmcheckout.ConfirmCheckoutResponse;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for order confirmation in checkout.
 *
 * <p>This controller handles the confirmation step of checkout where the customer
 * confirms their order and views the thank you page.
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

  public ConfirmationPageController(
      final ConfirmCheckoutInputPort confirmCheckoutInputPort,
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort) {
    this.confirmCheckoutInputPort = confirmCheckoutInputPort;
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
  }

  /**
   * Confirms the checkout and places the order.
   *
   * <p>This endpoint processes the order confirmation and redirects
   * to the confirmation page on success.
   *
   * @param id the checkout session ID
   * @param redirectAttributes for passing flash messages
   * @return redirect to confirmation page or back to review on error
   */
  @PostMapping("/{id}/confirm")
  public String confirmOrder(
      @PathVariable final String id, final RedirectAttributes redirectAttributes) {

    try {
      final ConfirmCheckoutResponse response =
          confirmCheckoutInputPort.execute(new ConfirmCheckoutCommand(id));

      redirectAttributes.addFlashAttribute("orderConfirmed", true);
      return "redirect:/checkout/" + response.sessionId() + "/confirmation";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/" + id + "/review";
    }
  }

  /**
   * Displays the order confirmation (thank you) page.
   *
   * <p>This endpoint shows the confirmation page after a successful order.
   * It is only accessible after the checkout has been confirmed.
   *
   * @param id the checkout session ID
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the confirmation.pug template or redirect on error
   */
  @GetMapping("/{id}/confirmation")
  public String showConfirmationPage(
      @PathVariable final String id,
      final Model model,
      final RedirectAttributes redirectAttributes) {

    final GetCheckoutSessionResponse session =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(id));

    if (!session.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/";
    }

    // Only allow access to confirmation page if the session is confirmed or completed
    if (!isConfirmedOrCompleted(session.status())) {
      redirectAttributes.addFlashAttribute("error", "Order has not been confirmed yet");
      return "redirect:/checkout/" + id + "/review";
    }

    model.addAttribute("session", session);
    model.addAttribute("title", "Order Confirmed - Thank You!");

    return "checkout/confirmation";
  }

  private boolean isConfirmedOrCompleted(final String status) {
    return "CONFIRMED".equals(status) || "COMPLETED".equals(status);
  }
}
