package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the order review page in checkout.
 *
 * <p>This controller handles the review step of checkout where the customer
 * reviews all order details before confirming the purchase.
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

  public ReviewPageController(final GetCheckoutSessionInputPort getCheckoutSessionInputPort) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
  }

  /**
   * Displays the order review page with all order details.
   *
   * <p>This endpoint retrieves the checkout session and displays all collected information
   * including buyer info, delivery details, payment method, and order summary for final
   * review before purchase confirmation.
   *
   * @param id the checkout session ID
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the review.pug template or redirect on error
   */
  @GetMapping("/{id}/review")
  public String showReviewPage(
      @PathVariable final String id,
      final Model model,
      final RedirectAttributes redirectAttributes) {

    final GetCheckoutSessionResponse session =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(id));

    if (!session.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/";
    }

    model.addAttribute("session", session);
    model.addAttribute("title", "Review Order - Checkout");

    return "checkout/review";
  }
}
