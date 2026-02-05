package de.sample.aiarchitecture.checkout.application.getcheckoutsession;

import java.math.BigDecimal;
import java.util.List;
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
public record GetCheckoutSessionResult(
    boolean found,
    @Nullable String sessionId,
    @Nullable String cartId,
    @Nullable String customerId,
    @Nullable String currentStep,
    @Nullable String status,
    List<LineItemData> lineItems,
    @Nullable TotalsData totals,
    @Nullable BuyerInfoData buyerInfo,
    @Nullable DeliveryData delivery,
    @Nullable PaymentData payment,
    @Nullable String orderReference) {

  /**
   * Creates a not-found response.
   *
   * @return a response indicating the session was not found
   */
  public static GetCheckoutSessionResult notFound() {
    return new GetCheckoutSessionResult(
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
  public record LineItemData(
      String id,
      String productId,
      String productName,
      BigDecimal unitPrice,
      String currencyCode,
      int quantity,
      BigDecimal lineTotal) {}

  /**
   * Checkout totals.
   *
   * @param subtotal the subtotal amount
   * @param shipping the shipping amount
   * @param tax the tax amount
   * @param total the total amount
   * @param currencyCode the currency code
   */
  public record TotalsData(
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal tax,
      BigDecimal total,
      String currencyCode) {}

  /**
   * Buyer information.
   *
   * @param email the buyer's email
   * @param firstName the buyer's first name
   * @param lastName the buyer's last name
   * @param phone the buyer's phone number
   */
  public record BuyerInfoData(
      String email,
      String firstName,
      String lastName,
      String phone) {}

  /**
   * Delivery information.
   *
   * @param address the delivery address
   * @param shippingOption the selected shipping option
   */
  public record DeliveryData(
      AddressData address, ShippingOptionData shippingOption) {}

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
  public record AddressData(
      String street,
      @Nullable String streetLine2,
      String city,
      String postalCode,
      String country,
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
  public record ShippingOptionData(
      String id,
      String name,
      String estimatedDelivery,
      BigDecimal cost,
      String currencyCode) {}

  /**
   * Payment selection.
   *
   * @param providerId the payment provider ID
   * @param providerReference the provider reference (optional)
   */
  public record PaymentData(String providerId, @Nullable String providerReference) {}
}
