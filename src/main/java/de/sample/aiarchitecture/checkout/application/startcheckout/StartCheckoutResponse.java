package de.sample.aiarchitecture.checkout.application.startcheckout;

import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Output model for checkout session creation.
 *
 * @param sessionId the generated checkout session ID
 * @param cartId the source cart ID
 * @param customerId the customer ID
 * @param currentStep the current checkout step
 * @param status the session status
 * @param lineItems the line items in the checkout
 * @param subtotal the subtotal amount as string
 */
public record StartCheckoutResponse(
    @NonNull String sessionId,
    @NonNull String cartId,
    @NonNull String customerId,
    @NonNull String currentStep,
    @NonNull String status,
    @NonNull List<LineItemResponse> lineItems,
    @NonNull String subtotal) {

  /**
   * Line item details in the response.
   *
   * @param lineItemId the line item ID
   * @param productId the product ID
   * @param productName the product name
   * @param unitPrice the unit price as string
   * @param quantity the quantity
   * @param lineTotal the line total as string
   */
  public record LineItemResponse(
      @NonNull String lineItemId,
      @NonNull String productId,
      @NonNull String productName,
      @NonNull String unitPrice,
      int quantity,
      @NonNull String lineTotal) {}
}
