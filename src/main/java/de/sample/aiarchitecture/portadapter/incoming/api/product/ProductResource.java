package de.sample.aiarchitecture.portadapter.incoming.api.product;

import de.sample.aiarchitecture.application.ProductApplicationService;
import de.sample.aiarchitecture.domain.model.product.*;
import de.sample.aiarchitecture.domain.model.shared.Money;
import de.sample.aiarchitecture.domain.model.shared.Price;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductResource {

  private final ProductApplicationService productApplicationService;
  private final ProductDtoConverter converter;

  public ProductResource(
      final ProductApplicationService productApplicationService,
      final ProductDtoConverter converter) {
    this.productApplicationService = productApplicationService;
    this.converter = converter;
  }

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(
      @Valid @RequestBody final CreateProductRequest request) {
    final Product product =
        productApplicationService.createProduct(
            SKU.of(request.sku()),
            ProductName.of(request.name()),
            ProductDescription.of(request.description() != null ? request.description() : ""),
            Price.of(Money.euro(request.price())),
            Category.of(request.category()),
            ProductStock.of(request.stock()));

    return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(product));
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    final List<ProductDto> products =
        productApplicationService.getAllProducts().stream()
            .map(converter::toDto)
            .toList();

    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable final String id) {
    return productApplicationService
        .findProductById(ProductId.of(id))
        .map(converter::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/sku/{sku}")
  public ResponseEntity<ProductDto> getProductBySku(@PathVariable final String sku) {
    return productApplicationService
        .findProductBySku(SKU.of(sku))
        .map(converter::toDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable final String id) {
    productApplicationService.deleteProduct(ProductId.of(id));
    return ResponseEntity.noContent().build();
  }
}
