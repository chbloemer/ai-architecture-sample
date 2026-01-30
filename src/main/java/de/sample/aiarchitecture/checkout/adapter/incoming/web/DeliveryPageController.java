package de.sample.aiarchitecture.checkout.adapter.incoming.web;

import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import de.sample.aiarchitecture.checkout.application.getcheckoutsession.GetCheckoutSessionResponse;
import de.sample.aiarchitecture.checkout.application.getshippingoptions.GetShippingOptionsInputPort;
import de.sample.aiarchitecture.checkout.application.getshippingoptions.GetShippingOptionsQuery;
import de.sample.aiarchitecture.checkout.application.getshippingoptions.GetShippingOptionsResponse;
import de.sample.aiarchitecture.checkout.application.submitdelivery.SubmitDeliveryCommand;
import de.sample.aiarchitecture.checkout.application.submitdelivery.SubmitDeliveryInputPort;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the delivery/shipping address page in checkout.
 *
 * <p>This controller handles the delivery step of checkout where the customer
 * enters their shipping address and selects a shipping option.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class DeliveryPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetShippingOptionsInputPort getShippingOptionsInputPort;
  private final SubmitDeliveryInputPort submitDeliveryInputPort;

  public DeliveryPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetShippingOptionsInputPort getShippingOptionsInputPort,
      final SubmitDeliveryInputPort submitDeliveryInputPort) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getShippingOptionsInputPort = getShippingOptionsInputPort;
    this.submitDeliveryInputPort = submitDeliveryInputPort;
  }

  /**
   * Displays the delivery address and shipping options form.
   *
   * <p>This endpoint retrieves the checkout session and available shipping options,
   * then displays the delivery form. If the session is not found, redirects to home with an error.
   *
   * @param id the checkout session ID
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the delivery.pug template or redirect on error
   */
  @GetMapping("/{id}/delivery")
  public String showDeliveryForm(
      @PathVariable final String id,
      final Model model,
      final RedirectAttributes redirectAttributes) {

    final GetCheckoutSessionResponse session =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(id));

    if (!session.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/";
    }

    final GetShippingOptionsResponse shippingOptions =
        getShippingOptionsInputPort.execute(GetShippingOptionsQuery.create());

    model.addAttribute("session", session);
    model.addAttribute("shippingOptions", shippingOptions.shippingOptions());
    model.addAttribute("title", "Delivery - Checkout");

    return "checkout/delivery";
  }

  /**
   * Submits delivery information and advances to payment step.
   *
   * <p>This endpoint processes the delivery form submission and redirects
   * to the payment step on success.
   *
   * @param id the checkout session ID
   * @param street the street address
   * @param streetLine2 optional second address line
   * @param city the city
   * @param postalCode the postal code
   * @param country the country
   * @param state optional state/province
   * @param shippingOptionId the selected shipping option ID
   * @param shippingOptionName the shipping option display name
   * @param estimatedDelivery the estimated delivery time
   * @param shippingCost the shipping cost
   * @param currencyCode the currency code for shipping cost
   * @param redirectAttributes for passing flash messages
   * @return redirect to payment step or back to delivery on error
   */
  @PostMapping("/{id}/delivery")
  public String submitDelivery(
      @PathVariable final String id,
      @RequestParam final String street,
      @RequestParam(required = false) final String streetLine2,
      @RequestParam final String city,
      @RequestParam final String postalCode,
      @RequestParam final String country,
      @RequestParam(required = false) final String state,
      @RequestParam final String shippingOptionId,
      @RequestParam final String shippingOptionName,
      @RequestParam final String estimatedDelivery,
      @RequestParam final BigDecimal shippingCost,
      @RequestParam final String currencyCode,
      final RedirectAttributes redirectAttributes) {

    try {
      submitDeliveryInputPort.execute(
          new SubmitDeliveryCommand(
              id,
              street,
              streetLine2,
              city,
              postalCode,
              country,
              state,
              shippingOptionId,
              shippingOptionName,
              estimatedDelivery,
              shippingCost,
              currencyCode));

      return "redirect:/checkout/" + id + "/payment";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/" + id + "/delivery";
    }
  }
}
