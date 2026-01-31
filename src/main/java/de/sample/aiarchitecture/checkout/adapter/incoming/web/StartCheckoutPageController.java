package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutCommand;
import de.sample.aiarchitecture.checkout.application.startcheckout.StartCheckoutInputPort;
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
 *   <li>Accepting the cart ID as a request parameter (passed from cart UI)</li>
 *   <li>Starting a new checkout session via the use case</li>
 *   <li>Redirecting to the buyer info step</li>
 * </ul>
 *
 * <p><b>Clean Architecture:</b> This controller only depends on use case interfaces from
 * its own bounded context (checkout), avoiding cross-context dependencies. The cart ID
 * is passed from the UI, which already has this information.
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
   * Initiates checkout from the specified cart.
   *
   * <p>The cart ID is passed as a request parameter from the cart UI,
   * avoiding the need to query the cart context directly.
   *
   * @param cartId the cart ID to start checkout from
   * @param redirectAttributes for passing flash messages on error
   * @return redirect to buyer info step at /checkout/buyer
   */
  @GetMapping("/start")
  public String startCheckout(
      @RequestParam final String cartId,
      final RedirectAttributes redirectAttributes) {

    try {
      startCheckoutInputPort.execute(new StartCheckoutCommand(cartId));
      return "redirect:/checkout/buyer";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cart";
    }
  }
}
