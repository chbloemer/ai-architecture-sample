package de.sample.aiarchitecture.product.infrastructure;

import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import de.sample.aiarchitecture.product.domain.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Product context domain services and factories.
 *
 * <p>Domain services and factories are framework-independent but need to be instantiated as Spring
 * beans so they can be injected into application services.
 */
@Configuration
public class ProductDomainConfiguration {

  @Bean
  public ProductFactory productFactory() {
    return new ProductFactory();
  }

  @Bean
  public PricingService pricingService() {
    return new PricingService();
  }
}
