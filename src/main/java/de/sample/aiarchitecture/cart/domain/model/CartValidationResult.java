package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object representing the result of cart validation for checkout.
 *
 * <p>Collects validation errors that prevent a cart from being checked out,
 * such as unavailable products or insufficient stock.
 */
public record CartValidationResult(List<ValidationError> errors) implements Value {

  public CartValidationResult {
    if (errors == null) {
      throw new IllegalArgumentException("Errors list cannot be null");
    }
    errors = Collections.unmodifiableList(new ArrayList<>(errors));
  }

  /**
   * Returns true if the cart is valid for checkout (no errors).
   *
   * @return true if no validation errors exist
   */
  public boolean isValid() {
    return errors.isEmpty();
  }

  /**
   * Creates a valid result with no errors.
   *
   * @return a CartValidationResult with an empty error list
   */
  public static CartValidationResult valid() {
    return new CartValidationResult(List.of());
  }

  /**
   * Creates a result with the specified errors.
   *
   * @param errors the validation errors
   * @return a CartValidationResult with the given errors
   */
  public static CartValidationResult withErrors(final List<ValidationError> errors) {
    return new CartValidationResult(errors);
  }

  /**
   * Represents a single validation error for a product in the cart.
   */
  public record ValidationError(
      ProductId productId,
      String message,
      ErrorType type) implements Value {

    public ValidationError {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
      if (message == null || message.isBlank()) {
        throw new IllegalArgumentException("Message cannot be null or blank");
      }
      if (type == null) {
        throw new IllegalArgumentException("ErrorType cannot be null");
      }
    }

    /**
     * Creates a validation error for an unavailable product.
     *
     * @param productId the unavailable product ID
     * @return a ValidationError of type PRODUCT_UNAVAILABLE
     */
    public static ValidationError productUnavailable(final ProductId productId) {
      return new ValidationError(
          productId,
          "Product is not available: " + productId.value(),
          ErrorType.PRODUCT_UNAVAILABLE);
    }

    /**
     * Creates a validation error for insufficient stock.
     *
     * @param productId the product ID
     * @param requested the requested quantity
     * @param available the available stock
     * @return a ValidationError of type INSUFFICIENT_STOCK
     */
    public static ValidationError insufficientStock(
        final ProductId productId,
        final int requested,
        final int available) {
      return new ValidationError(
          productId,
          String.format(
              "Insufficient stock for product %s: requested %d, available %d",
              productId.value(), requested, available),
          ErrorType.INSUFFICIENT_STOCK);
    }
  }

  /**
   * The type of validation error.
   */
  public enum ErrorType {
    /** The product is not available for purchase. */
    PRODUCT_UNAVAILABLE,
    /** There is not enough stock to fulfill the requested quantity. */
    INSUFFICIENT_STOCK
  }
}
