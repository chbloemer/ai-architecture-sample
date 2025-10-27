package de.sample.aiarchitecture.portadapter.incoming.api.product;

import de.sample.aiarchitecture.domain.model.product.Product;
import org.springframework.stereotype.Component;

@Component
public final class ProductDtoConverter {

  public ProductDto toDto(final Product product) {
    return new ProductDto(
        product.id().value(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.price().value().amount(),
        product.price().value().currency().getCurrencyCode(),
        product.category().name(),
        product.stock().quantity());
  }
}
