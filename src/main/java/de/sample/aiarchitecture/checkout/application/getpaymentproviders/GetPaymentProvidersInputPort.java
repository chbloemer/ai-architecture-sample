package de.sample.aiarchitecture.checkout.application.getpaymentproviders;

import de.sample.aiarchitecture.sharedkernel.application.port.UseCase;
import org.jspecify.annotations.NonNull;

/**
 * Input port for retrieving available payment providers.
 *
 * <p>This port defines the contract for querying payment methods.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see GetPaymentProvidersUseCase
 */
public interface GetPaymentProvidersInputPort
    extends UseCase<GetPaymentProvidersQuery, GetPaymentProvidersResponse> {

  /**
   * Retrieves all available payment providers.
   *
   * <p>Returns a list of payment providers with their availability status.
   *
   * @param query the query (marker, no parameters required)
   * @return response containing available payment providers
   */
  @Override
  @NonNull GetPaymentProvidersResponse execute(@NonNull GetPaymentProvidersQuery query);
}
