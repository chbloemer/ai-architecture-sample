package de.sample.aiarchitecture.cart.adapter.outgoing.persistence;

import de.sample.aiarchitecture.cart.application.port.out.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.*;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * H2/JDBC implementation of ShoppingCartRepository.
 *
 * <p>This adapter persists carts and items to an in-memory H2 database using Spring JDBC. It
 * reconstructs aggregates without emitting domain events by using reflection to set internal
 * state and then clears any collected events.
 */
@org.springframework.context.annotation.Profile("jdbc")
@Repository
public class JdbcShoppingCartRepository implements ShoppingCartRepository {

  private final JdbcTemplate jdbcTemplate;

  public JdbcShoppingCartRepository(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public Optional<ShoppingCart> findById(@NonNull final CartId id) {
    final List<ShoppingCart> carts =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE id = ?",
            cartRowMapper(),
            id.value());
    if (carts.isEmpty()) return Optional.empty();
    final ShoppingCart cart = carts.get(0);
    loadItems(cart);
    clearDomainEvents(cart);
    return Optional.of(cart);
  }

  @Override
  public List<ShoppingCart> findByCustomerId(@NonNull final CustomerId customerId) {
    final List<ShoppingCart> carts =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE customer_id = ? ORDER BY updated_at DESC",
            cartRowMapper(),
            customerId.value());
    carts.forEach(c -> { loadItems(c); clearDomainEvents(c);} );
    return carts;
  }

  @Override
  public Optional<ShoppingCart> findActiveCartByCustomerId(@NonNull final CustomerId customerId) {
    final List<ShoppingCart> carts =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts WHERE customer_id = ? AND status = ? ORDER BY updated_at DESC LIMIT 1",
            cartRowMapper(),
            customerId.value(),
            CartStatus.ACTIVE.name());
    if (carts.isEmpty()) return Optional.empty();
    final ShoppingCart cart = carts.get(0);
    loadItems(cart);
    clearDomainEvents(cart);
    return Optional.of(cart);
  }

  @Override
  public List<ShoppingCart> findAll() {
    final List<ShoppingCart> carts =
        jdbcTemplate.query(
            "SELECT id, customer_id, status FROM carts ORDER BY updated_at DESC",
            cartRowMapper());
    carts.forEach(c -> { loadItems(c); clearDomainEvents(c);} );
    return carts;
  }

  @Override
  @Transactional
  public ShoppingCart save(@NonNull final ShoppingCart cart) {
    // Upsert cart
    jdbcTemplate.update(
        "MERGE INTO carts (id, customer_id, status, updated_at) KEY(id) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
        cart.id().value(), cart.customerId().value(), cart.status().name());

    // Replace items
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", cart.id().value());

    for (final CartItem item : cart.items()) {
      jdbcTemplate.update(
          "INSERT INTO cart_items (id, cart_id, product_id, quantity, price_amount, price_currency) VALUES (?, ?, ?, ?, ?, ?)",
          item.id().value(),
          cart.id().value(),
          item.productId().value(),
          item.quantity().value(),
          item.priceAtAddition().value().amount(),
          item.priceAtAddition().value().currency().getCurrencyCode());
    }

    return cart;
  }

  @Override
  @Transactional
  public void deleteById(@NonNull final CartId id) {
    jdbcTemplate.update("DELETE FROM cart_items WHERE cart_id = ?", id.value());
    jdbcTemplate.update("DELETE FROM carts WHERE id = ?", id.value());
  }

  private RowMapper<ShoppingCart> cartRowMapper() {
    return (rs, rowNum) -> {
      final CartId id = CartId.of(rs.getString("id"));
      final CustomerId customerId = CustomerId.of(rs.getString("customer_id"));
      final ShoppingCart cart = new ShoppingCart(id, customerId);
      final String status = rs.getString("status");
      setStatus(cart, CartStatus.valueOf(status));
      return cart;
    };
  }

  private void loadItems(final ShoppingCart cart) {
    final List<Map<String, Object>> rows =
        jdbcTemplate.queryForList(
            "SELECT id, product_id, quantity, price_amount, price_currency FROM cart_items WHERE cart_id = ?",
            cart.id().value());
    if (rows.isEmpty()) return;
    try {
      final Field itemsField = ShoppingCart.class.getDeclaredField("items");
      itemsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      final List<CartItem> items = (List<CartItem>) itemsField.get(cart);

      final Constructor<CartItem> ctor =
          CartItem.class.getDeclaredConstructor(
              CartItemId.class, ProductId.class, Quantity.class, Price.class);
      ctor.setAccessible(true);

      for (final Map<String, Object> row : rows) {
        final CartItemId itemId = CartItemId.of((String) row.get("id"));
        final ProductId productId = ProductId.of((String) row.get("product_id"));
        final Quantity quantity = Quantity.of(((Number) row.get("quantity")).intValue());
        final String currency = (String) row.get("price_currency");
        final BigDecimal amount = (BigDecimal) row.get("price_amount");
        final Price price = Price.of(Money.of(amount, java.util.Currency.getInstance(currency)));

        final CartItem item = ctor.newInstance(itemId, productId, quantity, price);
        items.add(item);
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to reconstruct cart items via reflection", e);
    }
  }

  private void setStatus(final ShoppingCart cart, final CartStatus status) {
    try {
      final Field statusField = ShoppingCart.class.getDeclaredField("status");
      statusField.setAccessible(true);
      statusField.set(cart, status);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to set cart status via reflection", e);
    }
  }

  private void clearDomainEvents(final ShoppingCart cart) {
    cart.clearDomainEvents();
  }
}
