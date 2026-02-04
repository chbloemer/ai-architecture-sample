package de.sample.aiarchitecture.cart.domain.model;

import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import de.sample.aiarchitecture.sharedkernel.marker.tactical.Value;
import java.util.List;
import org.jspecify.annotations.NonNull;

/**
 * Value Object representing the result of validating a cart for checkout.
 *
 * <p>Collects validation errors that prevent a cart from being checked out, such as unavailable
 * products or insufficient stock.
 *
 * @param errors the list of validation errors, empty if validation passed
 */
public record CartValidationResult(@NonNull List<ValidationError> errors) implements Value {

  public CartValidationResult {
    errors = List.copyOf(errors);
  }

  /**
   * Returns whether the validation passed with no errors.
   *
   * @return true if there are no validation errors
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
   * @return a CartValidationResult containing the errors
   */
  public static CartValidationResult withErrors(@NonNull List<ValidationError> errors) {
    return new CartValidationResult(errors);
  }

  /**
   * Creates a result with a single error.
   *
   * @param error the validation error
   * @return a CartValidationResult containing the single error
   */
  public static CartValidationResult withError(@NonNull ValidationError error) {
    return new CartValidationResult(List.of(error));
  }

  /**
   * Value Object representing a single validation error for a product in the cart.
   *
   * @param productId the product that failed validation
   * @param message a human-readable error message
   * @param type the type of validation error
   */
  public record ValidationError(
      @NonNull ProductId productId, @NonNull String message, @NonNull ErrorType type)
      implements Value {

    public ValidationError {
      if (message == null || message.isBlank()) {
        throw new IllegalArgumentException("Error message cannot be null or blank");
      }
    }

    /**
     * Creates an error for an unavailable product.
     *
     * @param productId the unavailable product
     * @return a ValidationError with PRODUCT_UNAVAILABLE type
     */
    public static ValidationError productUnavailable(@NonNull ProductId productId) {
      return new ValidationError(
          productId, "Product is not available: " + productId.value(), ErrorType.PRODUCT_UNAVAILABLE);
    }

    /**
     * Creates an error for insufficient stock.
     *
     * @param productId the product with insufficient stock
     * @param requested the requested quantity
     * @param available the available quantity
     * @return a ValidationError with INSUFFICIENT_STOCK type
     */
    public static ValidationError insufficientStock(
        @NonNull ProductId productId, int requested, int available) {
      return new ValidationError(
          productId,
          String.format(
              "Insufficient stock for product %s: requested %d, available %d",
              productId.value(), requested, available),
          ErrorType.INSUFFICIENT_STOCK);
    }
  }

  /** Types of validation errors that can occur during cart validation. */
  public enum ErrorType {
    /** The product is not available for purchase. */
    PRODUCT_UNAVAILABLE,
    /** The requested quantity exceeds available stock. */
    INSUFFICIENT_STOCK
  }
}
