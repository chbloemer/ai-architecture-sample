package de.sample.aiarchitecture.cart.adapter.incoming.web;

import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartCommand;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartResponse;
import de.sample.aiarchitecture.cart.application.additemtocart.AddItemToCartUseCase;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartCommand;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartResponse;
import de.sample.aiarchitecture.cart.application.checkoutcart.CheckoutCartUseCase;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdQuery;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdResponse;
import de.sample.aiarchitecture.cart.application.getcartbyid.GetCartByIdUseCase;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartCommand;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartResponse;
import de.sample.aiarchitecture.cart.application.getorcreateactivecart.GetOrCreateActiveCartUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for rendering shopping cart pages using Pug templates.
 *
 * <p>This controller handles traditional server-side rendered pages using Pug4j templates.
 * Unlike REST API controllers which return JSON, this controller returns HTML views.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix. REST controllers use {@code @RestController} and end
 * with "Resource" suffix.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/cart/}
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 */
@Controller
@RequestMapping("/cart")
public class CartPageController {

  private final GetCartByIdUseCase getCartByIdUseCase;
  private final GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase;
  private final AddItemToCartUseCase addItemToCartUseCase;
  private final CheckoutCartUseCase checkoutCartUseCase;

  public CartPageController(
      final GetCartByIdUseCase getCartByIdUseCase,
      final GetOrCreateActiveCartUseCase getOrCreateActiveCartUseCase,
      final AddItemToCartUseCase addItemToCartUseCase,
      final CheckoutCartUseCase checkoutCartUseCase) {
    this.getCartByIdUseCase = getCartByIdUseCase;
    this.getOrCreateActiveCartUseCase = getOrCreateActiveCartUseCase;
    this.addItemToCartUseCase = addItemToCartUseCase;
    this.checkoutCartUseCase = checkoutCartUseCase;
  }

  /**
   * Displays the shopping cart page.
   *
   * <p>Returns the cart details rendered using the Pug template.
   *
   * @param cartId the cart ID from path variable
   * @param model Spring MVC model to pass data to the view
   * @return view name "cart/view" which resolves to templates/cart/view.pug
   */
  @GetMapping("/{cartId}")
  public String showCart(@PathVariable final String cartId, final Model model) {
    final GetCartByIdResponse output = getCartByIdUseCase.execute(new GetCartByIdQuery(cartId));

    if (!output.found()) {
      return "error/404";
    }

    model.addAttribute("cart", output);
    model.addAttribute("title", "Shopping Cart");
    model.addAttribute("cartId", cartId);
    model.addAttribute("itemCount", output.items().size());
    model.addAttribute("totalQuantity", output.items().stream()
        .mapToInt(item -> item.quantity())
        .sum());

    return "cart/view";
  }

  /**
   * Handles adding a product to the cart.
   *
   * <p>This endpoint:
   * <ul>
   *   <li>Gets or creates an active cart for the current session
   *   <li>Adds the specified product to the cart
   *   <li>Redirects to the cart view page
   * </ul>
   *
   * @param productId the product ID to add
   * @param quantity the quantity to add (default: 1)
   * @param session HTTP session to track customer cart
   * @param redirectAttributes for passing flash messages
   * @return redirect to cart view
   */
  @PostMapping("/add-product")
  public String addProductToCart(
      @RequestParam final String productId,
      @RequestParam(defaultValue = "1") final int quantity,
      final HttpSession session,
      final RedirectAttributes redirectAttributes) {

    // Get or create customer ID from session
    String customerId = (String) session.getAttribute("customerId");
    if (customerId == null) {
      customerId = "customer-" + session.getId();
      session.setAttribute("customerId", customerId);
    }

    // Get or create active cart
    final GetOrCreateActiveCartResponse cartResponse =
        getOrCreateActiveCartUseCase.execute(new GetOrCreateActiveCartCommand(customerId));

    // Add product to cart
    final AddItemToCartCommand command =
        new AddItemToCartCommand(cartResponse.cartId(), productId, quantity);
    final AddItemToCartResponse addResponse = addItemToCartUseCase.execute(command);

    // Add success message
    redirectAttributes.addFlashAttribute("message", "Product added to cart!");

    // Redirect to cart view
    return "redirect:/cart/" + cartResponse.cartId();
  }

  /**
   * Handles cart checkout.
   *
   * <p>This endpoint:
   * <ul>
   *   <li>Checks out the cart (changes status to CHECKED_OUT)
   *   <li>Publishes CartCheckedOut domain event (triggers stock reduction)
   *   <li>Redirects back to cart view with success message
   * </ul>
   *
   * @param cartId the cart ID to checkout
   * @param redirectAttributes for passing flash messages
   * @return redirect to cart view
   */
  @PostMapping("/{cartId}/checkout")
  public String checkoutCart(
      @PathVariable final String cartId,
      final RedirectAttributes redirectAttributes) {

    try {
      final CheckoutCartResponse response = checkoutCartUseCase.execute(new CheckoutCartCommand(cartId));

      redirectAttributes.addFlashAttribute("message",
          "Cart checked out successfully! Order total: " +
          response.totalAmount() + " " + response.totalCurrency());

    } catch (IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("error", "Cart not found");
    }

    return "redirect:/cart/" + cartId;
  }
}
