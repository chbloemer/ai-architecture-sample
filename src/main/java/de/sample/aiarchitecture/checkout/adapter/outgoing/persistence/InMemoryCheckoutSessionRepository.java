package de.sample.aiarchitecture.checkout.adapter.outgoing.persistence;

import de.sample.aiarchitecture.cart.domain.model.CartId;
import de.sample.aiarchitecture.checkout.application.shared.CheckoutSessionRepository;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSession;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionId;
import de.sample.aiarchitecture.checkout.domain.model.CheckoutSessionStatus;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of CheckoutSessionRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for checkout sessions using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 */
@org.springframework.context.annotation.Profile("inmemory")
@Repository
public class InMemoryCheckoutSessionRepository implements CheckoutSessionRepository {

  private final ConcurrentHashMap<CheckoutSessionId, CheckoutSession> sessions =
      new ConcurrentHashMap<>();

  @Override
  public Optional<CheckoutSession> findById(@NonNull final CheckoutSessionId id) {
    return Optional.ofNullable(sessions.get(id));
  }

  @Override
  public Optional<CheckoutSession> findByCartId(@NonNull final CartId cartId) {
    return sessions.values().stream()
        .filter(session -> session.cartId().equals(cartId))
        .findFirst();
  }

  @Override
  public List<CheckoutSession> findExpiredSessions() {
    return sessions.values().stream()
        .filter(session -> session.status() == CheckoutSessionStatus.EXPIRED)
        .toList();
  }

  @Override
  public List<CheckoutSession> findAll() {
    return List.copyOf(sessions.values());
  }

  @Override
  public CheckoutSession save(@NonNull final CheckoutSession session) {
    sessions.put(session.id(), session);
    return session;
  }

  @Override
  public void deleteById(@NonNull final CheckoutSessionId id) {
    sessions.remove(id);
  }
}
