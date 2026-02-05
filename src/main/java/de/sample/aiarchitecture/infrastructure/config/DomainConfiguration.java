package de.sample.aiarchitecture.infrastructure.config;

import de.sample.aiarchitecture.cart.domain.service.CartTotalCalculator;
import de.sample.aiarchitecture.product.domain.service.PricingService;
import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain-level services and factories.
 *
 * <p>Domain services and factories are framework-independent but need to be
 * instantiated as Spring beans so they can be injected into application services.
 */
@Configuration
public class DomainConfiguration {

  @Bean
  public ProductFactory productFactory() {
    return new ProductFactory();
  }

  @Bean
  public PricingService pricingService() {
    return new PricingService();
  }

  @Bean
  public CartTotalCalculator cartTotalCalculator() {
    return new CartTotalCalculator();
  }
}
