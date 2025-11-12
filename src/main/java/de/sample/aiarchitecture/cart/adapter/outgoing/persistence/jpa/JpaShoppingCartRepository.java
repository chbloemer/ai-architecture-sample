package de.sample.aiarchitecture.cart.adapter.outgoing.persistence.jpa;

import de.sample.aiarchitecture.cart.application.shared.ShoppingCartRepository;
import de.sample.aiarchitecture.cart.domain.model.*;
import de.sample.aiarchitecture.sharedkernel.domain.common.Money;
import de.sample.aiarchitecture.sharedkernel.domain.common.Price;
import de.sample.aiarchitecture.sharedkernel.domain.common.ProductId;
import de.sample.aiarchitecture.sharedkernel.domain.spec.CompositeSpecification;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Primary
public class JpaShoppingCartRepository implements ShoppingCartRepository {

  private final CartJpaRepository cartRepo;
  private final CartSpecToJpa specTranslator;

  public JpaShoppingCartRepository(final CartJpaRepository cartRepo, final CartSpecToJpa specTranslator) {
    this.cartRepo = cartRepo;
    this.specTranslator = specTranslator;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findById(@NonNull final CartId id) {
    return cartRepo.findById(id.value()).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingCart> findByCustomerId(@NonNull final CustomerId customerId) {
    return cartRepo.findByCustomerId(customerId.value()).stream().map(this::toDomain).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findActiveCartByCustomerId(@NonNull final CustomerId customerId) {
    return cartRepo
        .findFirstByCustomerIdAndStatusOrderByUpdatedAtDesc(customerId.value(), CartStatus.ACTIVE.name())
        .map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingCart> findAll() {
    return cartRepo.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ShoppingCart> findBy(@NonNull final CompositeSpecification<ShoppingCart> specification, @NonNull final Pageable pageable) {
    final CartSpecToJpa translator = requireTranslator();
    final Specification<CartEntity> jpaSpec = specification.accept(translator);
    return cartRepo.findAll(jpaSpec, pageable).map(this::toDomain);
  }

  @Override
  @Transactional
  public ShoppingCart save(@NonNull final ShoppingCart cart) {
    final CartEntity entity = toEntity(cart);
    entity.setUpdatedAt(Instant.now());
    final CartEntity saved = cartRepo.saveAndFlush(entity);
    return toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(@NonNull final CartId id) {
    cartRepo.deleteById(id.value());
  }

  private CartSpecToJpa requireTranslator() {
    if (this.specTranslator == null) {
      throw new IllegalStateException("CartSpecToJpa translator not configured");
    }
    return this.specTranslator;
  }

  private ShoppingCart toDomain(final CartEntity entity) {
    final ShoppingCart cart = new ShoppingCart(CartId.of(entity.getId()), CustomerId.of(entity.getCustomerId()));
    setStatus(cart, CartStatus.valueOf(entity.getStatus()));

    if (entity.getItems() != null && !entity.getItems().isEmpty()) {
      try {
        final Field itemsField = ShoppingCart.class.getDeclaredField("items");
        itemsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<CartItem> items = (List<CartItem>) itemsField.get(cart);

        final Constructor<CartItem> ctor =
            CartItem.class.getDeclaredConstructor(
                CartItemId.class, ProductId.class, Quantity.class, Price.class);
        ctor.setAccessible(true);

        for (final CartItemEntity it : entity.getItems()) {
          final CartItemId itemId = CartItemId.of(it.getId());
          final ProductId productId = ProductId.of(it.getProductId());
          final Quantity quantity = Quantity.of(it.getQuantity());
          final Price price = Price.of(Money.of(it.getPriceAmount(), java.util.Currency.getInstance(it.getPriceCurrency())));
          final CartItem item = ctor.newInstance(itemId, productId, quantity, price);
          items.add(item);
        }
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to reconstruct cart items via reflection", e);
      }
    }

    cart.clearDomainEvents();
    return cart;
  }

  private CartEntity toEntity(final ShoppingCart cart) {
    final CartEntity e = new CartEntity();
    e.setId(cart.id().value());
    e.setCustomerId(cart.customerId().value());
    e.setStatus(cart.status().name());

    final List<CartItemEntity> itemEntities = new ArrayList<>();
    for (final CartItem item : cart.items()) {
      final CartItemEntity ie = new CartItemEntity();
      ie.setId(item.id().value());
      ie.setCart(e);
      ie.setProductId(item.productId().value());
      ie.setQuantity(item.quantity().value());
      ie.setPriceAmount(item.priceAtAddition().value().amount());
      ie.setPriceCurrency(item.priceAtAddition().value().currency().getCurrencyCode());
      itemEntities.add(ie);
    }
    e.setItems(itemEntities);
    return e;
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
}
