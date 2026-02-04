package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing the outcome of cart validation for checkout.
 *
 * <p>Collects validation errors that prevent a cart from being checked out,
 * such as unavailable products or insufficient stock.
 */
public record CartValidationOutcome(@NonNull List<ValidationError> errors) implements Value {

  public CartValidationOutcome {
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
   * Creates a valid outcome with no errors.
   *
   * @return a CartValidationOutcome with an empty error list
   */
  public static CartValidationOutcome valid() {
    return new CartValidationOutcome(List.of());
  }

  /**
   * Creates an outcome with the specified errors.
   *
   * @param errors the validation errors
   * @return a CartValidationOutcome with the given errors
   */
  public static CartValidationOutcome withErrors(@NonNull final List<ValidationError> errors) {
    return new CartValidationOutcome(errors);
  }

  /**
   * Represents a single validation error for a product in the cart.
   */
  public record ValidationError(
      @NonNull ProductId productId,
      @NonNull String message,
      @NonNull ErrorType type) implements Value {

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
    public static ValidationError productUnavailable(@NonNull final ProductId productId) {
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
        @NonNull final ProductId productId,
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
