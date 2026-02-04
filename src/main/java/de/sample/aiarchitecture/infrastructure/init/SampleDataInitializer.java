package de.sample.aiarchitecture.infrastructure.init;

import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.product.domain.model.ProductFactory;
import de.sample.aiarchitecture.product.domain.model.ProductName;
import de.sample.aiarchitecture.product.domain.model.SKU;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Initializes sample product data for demonstration purposes.
 *
 * <p>This component loads sample products into repositories on application startup.
 * It directly creates entries in all related bounded contexts since @PostConstruct
 * does not run within a transaction (so TransactionalEventListener won't fire).
 *
 * <p><b>Infrastructure Layer:</b> This component lives in the infrastructure layer because
 * it needs to coordinate across multiple bounded contexts (Product, Pricing, Inventory) for
 * sample data initialization. This is acceptable for demo/test data setup.
 */
@Component
public class SampleDataInitializer {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final ProductPriceRepository productPriceRepository;
  private final StockLevelRepository stockLevelRepository;

  public SampleDataInitializer(
      final ProductRepository productRepository,
      final ProductFactory productFactory,
      final ProductPriceRepository productPriceRepository,
      final StockLevelRepository stockLevelRepository) {
    this.productRepository = productRepository;
    this.productFactory = productFactory;
    this.productPriceRepository = productPriceRepository;
    this.stockLevelRepository = stockLevelRepository;
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
      final int initialStock) {

    final Money initialPrice = Money.euro(price);

    // Create product (stock is not stored in Product - managed by Inventory context)
    final Product product =
        productFactory.createProduct(
            SKU.of(sku),
            ProductName.of(name),
            ProductDescription.of(description),
            category,
            initialPrice,
            initialStock);

    productRepository.save(product);

    // Directly create price entry in Pricing context
    // (We do this directly because @PostConstruct doesn't run in a transaction,
    // so @TransactionalEventListener won't fire for the ProductCreated event)
    ProductPrice productPrice = ProductPrice.create(product.id(), initialPrice);
    productPriceRepository.save(productPrice);

    // Directly create stock level entry in Inventory context
    // (We do this directly because @PostConstruct doesn't run in a transaction)
    StockLevel stockLevel = StockLevel.create(product.id(), initialStock);
    stockLevelRepository.save(stockLevel);
  }
}
