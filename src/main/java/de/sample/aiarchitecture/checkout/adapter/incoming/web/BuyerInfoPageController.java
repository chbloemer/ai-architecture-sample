package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResponse;
import de.sample.aiarchitecture.checkout.application.submitbuyerinfo.SubmitBuyerInfoCommand;
import de.sample.aiarchitecture.checkout.application.submitbuyerinfo.SubmitBuyerInfoInputPort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the buyer information page in checkout.
 *
 * <p>This controller handles the buyer info step of checkout where the customer
 * enters their contact information (email, name, phone).
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class BuyerInfoPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final SubmitBuyerInfoInputPort submitBuyerInfoInputPort;

  public BuyerInfoPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final SubmitBuyerInfoInputPort submitBuyerInfoInputPort) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.submitBuyerInfoInputPort = submitBuyerInfoInputPort;
  }

  /**
   * Displays the buyer information form.
   *
   * <p>This endpoint retrieves the checkout session and displays the buyer info form.
   * If the session is not found, redirects to home with an error.
   *
   * @param id the checkout session ID
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the buyer.pug template or redirect on error
   */
  @GetMapping("/{id}/buyer")
  public String showBuyerInfoForm(
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
    model.addAttribute("title", "Buyer Information - Checkout");

    return "checkout/buyer";
  }

  /**
   * Submits buyer information and advances to delivery step.
   *
   * <p>This endpoint processes the buyer info form submission and redirects
   * to the delivery step on success.
   *
   * @param id the checkout session ID
   * @param email the buyer's email address
   * @param firstName the buyer's first name
   * @param lastName the buyer's last name
   * @param phone the buyer's phone number
   * @param redirectAttributes for passing flash messages
   * @return redirect to delivery step or back to buyer info on error
   */
  @PostMapping("/{id}/buyer")
  public String submitBuyerInfo(
      @PathVariable final String id,
      @RequestParam final String email,
      @RequestParam final String firstName,
      @RequestParam final String lastName,
      @RequestParam final String phone,
      final RedirectAttributes redirectAttributes) {

    try {
      submitBuyerInfoInputPort.execute(
          new SubmitBuyerInfoCommand(id, email, firstName, lastName, phone));

      return "redirect:/checkout/" + id + "/delivery";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/" + id + "/buyer";
    }
  }
}
