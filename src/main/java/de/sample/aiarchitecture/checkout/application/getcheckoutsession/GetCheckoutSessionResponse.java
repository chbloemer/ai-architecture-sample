package de.sample.aiarchitecture.checkout.application.getcheckoutsession;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Output model containing checkout session data for display.
 *
 * @param found whether the session was found
 * @param sessionId the session ID (null if not found)
 * @param cartId the cart ID (null if not found)
 * @param customerId the customer ID (null if not found)
 * @param currentStep the current checkout step (null if not found)
 * @param status the session status (null if not found)
 * @param lineItems the line items (empty if not found)
 * @param totals the checkout totals (null if not found)
 * @param buyerInfo the buyer information (null if not submitted or not found)
 * @param delivery the delivery information (null if not submitted or not found)
 * @param payment the payment selection (null if not submitted or not found)
 * @param orderReference the order reference (null if not completed or not found)
 */
public record GetCheckoutSessionResponse(
    boolean found,
    @Nullable String sessionId,
    @Nullable String cartId,
    @Nullable String customerId,
    @Nullable String currentStep,
    @Nullable String status,
    @NonNull List<LineItemResponse> lineItems,
    @Nullable TotalsResponse totals,
    @Nullable BuyerInfoResponse buyerInfo,
    @Nullable DeliveryResponse delivery,
    @Nullable PaymentResponse payment,
    @Nullable String orderReference) {

  /**
   * Creates a not-found response.
   *
   * @return a response indicating the session was not found
   */
  public static GetCheckoutSessionResponse notFound() {
    return new GetCheckoutSessionResponse(
        false, null, null, null, null, null, List.of(), null, null, null, null, null);
  }

  /**
   * Line item details.
   *
   * @param id the line item ID
   * @param productId the product ID
   * @param productName the product name
   * @param unitPrice the unit price amount
   * @param currencyCode the currency code
   * @param quantity the quantity
   * @param lineTotal the line total amount
   */
  public record LineItemResponse(
      @NonNull String id,
      @NonNull String productId,
      @NonNull String productName,
      @NonNull BigDecimal unitPrice,
      @NonNull String currencyCode,
      int quantity,
      @NonNull BigDecimal lineTotal) {}

  /**
   * Checkout totals.
   *
   * @param subtotal the subtotal amount
   * @param shipping the shipping amount
   * @param tax the tax amount
   * @param total the total amount
   * @param currencyCode the currency code
   */
  public record TotalsResponse(
      @NonNull BigDecimal subtotal,
      @NonNull BigDecimal shipping,
      @NonNull BigDecimal tax,
      @NonNull BigDecimal total,
      @NonNull String currencyCode) {}

  /**
   * Buyer information.
   *
   * @param email the buyer's email
   * @param firstName the buyer's first name
   * @param lastName the buyer's last name
   * @param phone the buyer's phone number
   */
  public record BuyerInfoResponse(
      @NonNull String email,
      @NonNull String firstName,
      @NonNull String lastName,
      @NonNull String phone) {}

  /**
   * Delivery information.
   *
   * @param address the delivery address
   * @param shippingOption the selected shipping option
   */
  public record DeliveryResponse(
      @NonNull AddressResponse address, @NonNull ShippingOptionResponse shippingOption) {}

  /**
   * Delivery address.
   *
   * @param street the street address
   * @param streetLine2 the second street line (optional)
   * @param city the city
   * @param postalCode the postal code
   * @param country the country
   * @param state the state (optional)
   */
  public record AddressResponse(
      @NonNull String street,
      @Nullable String streetLine2,
      @NonNull String city,
      @NonNull String postalCode,
      @NonNull String country,
      @Nullable String state) {}

  /**
   * Shipping option details.
   *
   * @param id the shipping option ID
   * @param name the display name
   * @param estimatedDelivery the estimated delivery timeframe
   * @param cost the shipping cost
   * @param currencyCode the currency code
   */
  public record ShippingOptionResponse(
      @NonNull String id,
      @NonNull String name,
      @NonNull String estimatedDelivery,
      @NonNull BigDecimal cost,
      @NonNull String currencyCode) {}

  /**
   * Payment selection.
   *
   * @param providerId the payment provider ID
   * @param providerReference the provider reference (optional)
   */
  public record PaymentResponse(@NonNull String providerId, @Nullable String providerReference) {}
}
