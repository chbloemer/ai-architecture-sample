package de.sample.aiarchitecture.cart.infrastructure;

import de.sample.aiarchitecture.cart.domain.service.CartTotalCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Cart context domain services.
 *
 * <p>Domain services are framework-independent but need to be instantiated as Spring beans so they
 * can be injected into application services.
 */
@Configuration
public class CartDomainConfiguration {

  @Bean
  public CartTotalCalculator cartTotalCalculator() {
    return new CartTotalCalculator();
  }
}
