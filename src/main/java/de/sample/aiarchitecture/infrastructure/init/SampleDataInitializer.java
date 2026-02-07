package de.sample.aiarchitecture.infrastructure.init;

import de.sample.aiarchitecture.inventory.application.shared.StockLevelRepository;
import de.sample.aiarchitecture.inventory.domain.model.StockLevel;
import de.sample.aiarchitecture.pricing.application.shared.ProductPriceRepository;
import de.sample.aiarchitecture.pricing.domain.model.ProductPrice;
import de.sample.aiarchitecture.product.application.shared.ProductRepository;
import de.sample.aiarchitecture.product.domain.model.Category;
import de.sample.aiarchitecture.product.domain.model.Product;
import de.sample.aiarchitecture.product.domain.model.ProductDescription;
import de.sample.aiarchitecture.product.domain.model.ImageUrl;
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
        "Unleash your productivity with this high-performance laptop featuring 16GB RAM, a blazing-fast 512GB SSD, and a stunning 15.6-inch Retina display. Built for professionals who demand power and portability, it delivers all-day battery life and whisper-quiet operation.",
        "/images/products/laptop.svg",
        1299.99,
        Category.electronics(),
        15);

    createAndSaveProduct(
        "PHONE-001",
        "Smartphone Pro",
        "Capture every moment in breathtaking detail with our flagship smartphone. The triple-lens 108MP camera system, edge-to-edge AMOLED display, and 5G connectivity make this the ultimate mobile companion for work and play.",
        "/images/products/smartphone.svg",
        899.99,
        Category.electronics(),
        25);

    createAndSaveProduct(
        "TABLET-001",
        "Tablet Air",
        "The perfect blend of power and portability. This ultra-lightweight tablet features a vibrant 11-inch display, Apple M2 chip, and supports stylus input for creative professionals. Ideal for sketching, note-taking, and streaming on the go.",
        "/images/products/tablet.svg",
        599.99,
        Category.electronics(),
        30);

    // Clothing
    createAndSaveProduct(
        "SHIRT-001",
        "Cotton T-Shirt",
        "Made from 100% organic combed cotton, this premium t-shirt offers unmatched softness and breathability. Available in 12 vibrant colors with a modern relaxed fit that looks great whether you dress it up or keep it casual.",
        "/images/products/tshirt.svg",
        29.99,
        Category.clothing(),
        100);

    createAndSaveProduct(
        "JEANS-001",
        "Classic Jeans",
        "Crafted from premium selvedge denim with a classic straight-leg fit that never goes out of style. Features reinforced stitching, copper rivets, and a comfortable mid-rise waist. These jeans only get better with age.",
        "/images/products/jeans.svg",
        79.99,
        Category.clothing(),
        50);

    // Books
    createAndSaveProduct(
        "BOOK-001",
        "Domain-Driven Design",
        "The seminal work by Eric Evans that introduced the software industry to Domain-Driven Design. This essential guide teaches you how to tackle complexity in the heart of software by connecting implementation to an evolving model of the business domain.",
        "/images/products/ddd-book.svg",
        54.99,
        Category.books(),
        20);

    createAndSaveProduct(
        "BOOK-002",
        "Clean Architecture",
        "Robert C. Martin's definitive guide to software structure and design. Learn the universal rules of software architecture that dramatically improve developer productivity throughout the life of any software system.",
        "/images/products/clean-architecture-book.svg",
        39.99,
        Category.books(),
        35);

    // Home & Garden
    createAndSaveProduct(
        "CHAIR-001",
        "Ergonomic Office Chair",
        "Designed in collaboration with orthopedic specialists, this premium office chair features adjustable lumbar support, breathable mesh back, and a 4D armrest system. Work in comfort for hours with proper spinal alignment and pressure distribution.",
        "/images/products/office-chair.svg",
        299.99,
        Category.homeAndGarden(),
        12);

    createAndSaveProduct(
        "DESK-001",
        "Standing Desk",
        "Transform your workspace with this electric height-adjustable standing desk. Smooth dual-motor system transitions between sitting and standing in seconds, with programmable memory presets. The spacious 60x30 inch bamboo surface provides plenty of room for dual monitors.",
        "/images/products/standing-desk.svg",
        499.99,
        Category.homeAndGarden(),
        8);

    // Sports
    createAndSaveProduct(
        "YOGA-001",
        "Yoga Mat Premium",
        "Elevate your practice with this professional-grade yoga mat. The dual-layer design provides superior cushioning and a non-slip surface that grips better the more you sweat. Includes a cotton carrying strap and is made from eco-friendly, biodegradable natural rubber.",
        "/images/products/yoga-mat.svg",
        49.99,
        Category.sports(),
        40);

    createAndSaveProduct(
        "DUMBBELL-001",
        "Adjustable Dumbbells Set",
        "Replace an entire rack of weights with one smart set. These space-saving adjustable dumbbells let you switch between 5kg and 25kg in seconds with a simple twist-lock mechanism. Perfect for home workouts with professional-grade cast iron construction.",
        "/images/products/dumbbells.svg",
        199.99,
        Category.sports(),
        18);

    System.out.println("Sample data initialized: " + productRepository.findAll().size() + " products loaded");
  }

  private void createAndSaveProduct(
      final String sku,
      final String name,
      final String description,
      final String imageUrl,
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
            ImageUrl.of(imageUrl),
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
