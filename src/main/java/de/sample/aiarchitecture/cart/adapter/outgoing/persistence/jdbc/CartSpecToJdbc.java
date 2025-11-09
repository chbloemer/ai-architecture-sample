package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jdbc;

import de.sample.aiarchitecture.cart.domain.model.CartStatus;
import de.sample.aiarchitecture.cart.domain.model.ShoppingCart;
import de.sample.aiarchitecture.cart.domain.spec.*;
import de.sample.aiarchitecture.sharedkernel.domain.spec.AndSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.spec.CompositeSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.spec.NotSpecification;
import de.sample.aiarchitecture.sharedkernel.domain.spec.OrSpecification;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * Translates domain cart specifications into JDBC WHERE fragments with bind parameters.
 *
 * <p>The resulting SQL snippets are intended to be appended to a base query that selects from
 * the {@code carts} table using the alias {@code c}. Item-related predicates use correlated
 * subqueries against {@code cart_items}.
 */
@Component
public class CartSpecToJdbc implements CartSpecificationVisitor<CartSpecToJdbc.JdbcPredicate> {

  public record JdbcPredicate(String sql, List<Object> params) {
    public static JdbcPredicate alwaysTrue() { return new JdbcPredicate("1=1", List.of()); }
  }

  // ---- Leaf specs

  @Override
  public JdbcPredicate visit(@NonNull ActiveCart spec) {
    return new JdbcPredicate("c.status = ?", List.of(CartStatus.ACTIVE.name()));
  }

  @Override
  public JdbcPredicate visit(@NonNull LastUpdatedBefore spec) {
    return new JdbcPredicate("c.updated_at < ?", List.of(spec.threshold()));
  }

  @Override
  public JdbcPredicate visit(@NonNull HasMinTotal spec) {
    // Sum over items of this cart filtered by currency
    final String sql = "(SELECT COALESCE(SUM(ci.price_amount * ci.quantity), 0) FROM cart_items ci " +
        "WHERE ci.cart_id = c.id AND ci.price_currency = ?) >= ?";
    final List<Object> params = new ArrayList<>();
    params.add(spec.minimum().currency().getCurrencyCode());
    params.add(spec.minimum().amount());
    return new JdbcPredicate(sql, params);
  }

  @Override
  public JdbcPredicate visit(@NonNull HasAnyAvailableItem spec) {
    final String sql = "EXISTS (SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.quantity > 0)";
    return new JdbcPredicate(sql, List.of());
  }

  @Override
  public JdbcPredicate visit(@NonNull CustomerAllowsMarketing spec) {
    // No customer read-model currently; treat as no-op
    return JdbcPredicate.alwaysTrue();
  }

  // ---- Combinators

  @Override
  public JdbcPredicate visit(@NonNull AndSpecification<ShoppingCart> spec) {
    final JdbcPredicate l = dispatch(spec.left());
    final JdbcPredicate r = dispatch(spec.right());
    return combine("AND", l, r);
  }

  @Override
  public JdbcPredicate visit(@NonNull OrSpecification<ShoppingCart> spec) {
    final JdbcPredicate l = dispatch(spec.left());
    final JdbcPredicate r = dispatch(spec.right());
    return combine("OR", l, r);
  }

  @Override
  public JdbcPredicate visit(@NonNull NotSpecification<ShoppingCart> spec) {
    final JdbcPredicate inner = dispatch(spec.inner());
    final String sql = "NOT (" + inner.sql + ")";
    return new JdbcPredicate(sql, inner.params);
  }

  private JdbcPredicate combine(String op, JdbcPredicate l, JdbcPredicate r) {
    final List<Object> params = new ArrayList<>(l.params);
    params.addAll(r.params);
    return new JdbcPredicate("(" + l.sql + ") " + op + " (" + r.sql + ")", params);
  }

  private JdbcPredicate dispatch(CompositeSpecification<ShoppingCart> s) {
    return s.accept(this);
  }
}
