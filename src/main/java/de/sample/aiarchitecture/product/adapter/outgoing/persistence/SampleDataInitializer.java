package de.sample.aiarchitecture.product.adapter.outgoing.persistence;

import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.product.domain.model.ProductStock;
import de.sample.aiarchitecture.product.domain.model.SKU;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.Price;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Initializes sample product data for demonstration purposes.
 *
 * <p>This component loads sample products into the repository on application startup.
 */
@Component
public class SampleDataInitializer {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;

  public SampleDataInitializer(
      final ProductRepository productRepository, final ProductFactory productFactory) {
    this.productRepository = productRepository;
    this.productFactory = productFactory;
  }

  @PostConstruct
  public void initialize() {
    loadSampleProducts();
  }

  private void loadSampleProducts() {
    // Electronics
    createAndSaveProduct(
        "LAPTOP-001",
        "Professional Laptop",
        "High-performance laptop with 16GB RAM and 512GB SSD",
        1299.99,
        Category.electronics(),
        15);

    createAndSaveProduct(
        "PHONE-001",
        "Smartphone Pro",
        "Latest smartphone with amazing camera and display",
        899.99,
        Category.electronics(),
        25);

    createAndSaveProduct(
        "TABLET-001",
        "Tablet Air",
        "Lightweight tablet perfect for work and entertainment",
        599.99,
        Category.electronics(),
        30);

    // Clothing
    createAndSaveProduct(
        "SHIRT-001",
        "Cotton T-Shirt",
        "Comfortable cotton t-shirt in various colors",
        29.99,
        Category.clothing(),
        100);

    createAndSaveProduct(
        "JEANS-001",
        "Classic Jeans",
        "Classic fit jeans made from premium denim",
        79.99,
        Category.clothing(),
        50);

    // Books
    createAndSaveProduct(
        "BOOK-001",
        "Domain-Driven Design",
        "Essential guide to DDD by Eric Evans",
        54.99,
        Category.books(),
        20);

    createAndSaveProduct(
        "BOOK-002",
        "Clean Architecture",
        "A craftsman's guide to software structure and design",
        39.99,
        Category.books(),
        35);

    // Home & Garden
    createAndSaveProduct(
        "CHAIR-001",
        "Ergonomic Office Chair",
        "Comfortable chair with lumbar support",
        299.99,
        Category.homeAndGarden(),
        12);

    createAndSaveProduct(
        "DESK-001",
        "Standing Desk",
        "Adjustable height standing desk",
        499.99,
        Category.homeAndGarden(),
        8);

    // Sports
    createAndSaveProduct(
        "YOGA-001",
        "Yoga Mat Premium",
        "Non-slip yoga mat with carrying strap",
        49.99,
        Category.sports(),
        40);

    createAndSaveProduct(
        "DUMBBELL-001",
        "Adjustable Dumbbells Set",
        "Space-saving adjustable dumbbell set 5-25kg",
        199.99,
        Category.sports(),
        18);

    System.out.println("✓ Sample data initialized: " + productRepository.findAll().size() + " products loaded");
  }

  private void createAndSaveProduct(
      final String sku,
      final String name,
      final String description,
      final double price,
      final Category category,
      final int stock) {

    final Product product =
        productFactory.createProduct(
            SKU.of(sku),
            ProductName.of(name),
            ProductDescription.of(description),
            Price.of(Money.euro(price)),
            category,
            ProductStock.of(stock));

    productRepository.save(product);
  }
}
