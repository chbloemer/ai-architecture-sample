package de.sample.aiarchitecture.checkout.application.shared;

import de.sample.aiarchitecture.checkout.domain.model.PaymentProviderId;
import de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

/**
 * Output port for managing payment provider registration and lookup.
 *
 * <p>Acts as a registry for available payment providers, allowing the checkout system
 * to discover and select appropriate providers at runtime. This supports a plugin
 * architecture where new payment providers can be added without modifying core checkout logic.
 *
 * <p>Implementations of this interface manage the collection of registered
 * {@link PaymentProvider} instances and provide methods to query them.
 */
public interface PaymentProviderRegistry extends OutputPort {

  /**
   * Finds a payment provider by its unique identifier.
   *
   * @param providerId the payment provider ID to look up
   * @return the payment provider if found, empty otherwise
   */
  Optional<PaymentProvider> findById(@NonNull PaymentProviderId providerId);

  /**
   * Returns all registered payment providers.
   *
   * @return list of all registered providers (never null, may be empty)
   */
  @NonNull
  List<PaymentProvider> findAll();

  /**
   * Returns all payment providers that are currently available for processing.
   *
   * <p>This filters out providers that are temporarily unavailable (e.g., due to
   * maintenance or configuration issues).
   *
   * @return list of available providers (never null, may be empty)
   */
  @NonNull
  List<PaymentProvider> findAvailable();

  /**
   * Registers a new payment provider with the registry.
   *
   * <p>If a provider with the same ID is already registered, it will be replaced.
   *
   * @param provider the provider to register
   */
  void register(@NonNull PaymentProvider provider);

  /**
   * Removes a payment provider from the registry.
   *
   * @param providerId the ID of the provider to remove
   * @return true if the provider was removed, false if it was not found
   */
  boolean unregister(@NonNull PaymentProviderId providerId);

  /**
   * Checks if a provider with the given ID is registered.
   *
   * @param providerId the payment provider ID to check
   * @return true if registered, false otherwise
   */
  boolean isRegistered(@NonNull PaymentProviderId providerId);
}
