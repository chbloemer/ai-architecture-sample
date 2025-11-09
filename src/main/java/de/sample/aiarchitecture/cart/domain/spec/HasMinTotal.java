package de.sample.aiarchitecture.cart.domain.spec;

import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.spec.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.spec.SpecificationVisitor;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * Cart total (sum of item price * quantity) is greater than or equal to the given minimum.
 */
public record HasMinTotal(@NonNull Money minimum) implements CartSpecification {
  public HasMinTotal {
    Objects.requireNonNull(minimum, "minimum must not be null");
  }

  @Override
  public boolean isSatisfiedBy(@NonNull ShoppingCart candidate) {
    final Money total = candidate.calculateTotal();
    if (!total.currency().equals(minimum.currency())) {
      return false; // different currencies cannot be compared here
    }
    return total.amount().compareTo(minimum.amount()) >= 0;
  }

  @Override
  public <R> R accept(@NonNull SpecificationVisitor<ShoppingCart, R> visitor) {
    if (visitor instanceof CartSpecificationVisitor<?> v) {
      @SuppressWarnings("unchecked")
      final CartSpecificationVisitor<R> cv = (CartSpecificationVisitor<R>) v;
      return cv.visit(this);
    }
    return visitor.visit(new AndSpecification<>(this, this));
  }
}
