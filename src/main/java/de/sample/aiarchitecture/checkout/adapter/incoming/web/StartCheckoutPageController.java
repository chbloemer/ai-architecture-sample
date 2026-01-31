package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutInputPort;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for initiating checkout from a cart.
 *
 * <p>This controller handles the checkout initiation flow by:
 * <ul>
 *   <li>Receiving a cart ID parameter</li>
 *   <li>Starting a new checkout session via the use case</li>
 *   <li>Redirecting to the buyer info step</li>
 * </ul>
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class StartCheckoutPageController {

  private final StartCheckoutInputPort startCheckoutInputPort;

  public StartCheckoutPageController(final StartCheckoutInputPort startCheckoutInputPort) {
    this.startCheckoutInputPort = startCheckoutInputPort;
  }

  /**
   * Initiates checkout from a cart.
   *
   * <p>This endpoint creates a new checkout session from the specified cart
   * and redirects to the buyer info step.
   *
   * @param cartId the cart ID to checkout
   * @param redirectAttributes for passing flash messages on error
   * @return redirect to buyer info step at /checkout/{sessionId}/buyer
   */
  @GetMapping("/start")
  public String startCheckout(
      @RequestParam final String cartId,
      final RedirectAttributes redirectAttributes) {

    try {
      final StartCheckoutResponse response = startCheckoutInputPort.execute(
          new StartCheckoutCommand(cartId));

      return "redirect:/checkout/" + response.sessionId() + "/buyer";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cart";
    }
  }
}
