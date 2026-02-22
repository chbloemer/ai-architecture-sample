package de.sample.aiarchitecture.infrastructure.init;

import de.sample.aiarchitecture.inventory.api.InventoryService;
import de.sample.aiarchitecture.pricing.api.PricingService;
import de.sample.aiarchitecture.product.api.ProductCatalogService;
import de.sample.aiarchitecture.product.api.ProductCatalogService.CreatedProduct;
import de.sample.aiarchitecture.sharedkernel.domain.model.Money;
import de.sample.aiarchitecture.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Initializes sample product data for demonstration purposes.
 *
 * <p>Uses the published APIs of Product, Pricing, and Inventory contexts to create products with
 * initial prices and stock levels. This cross-cutting initializer lives in infrastructure because
 * it orchestrates multiple bounded contexts.
 *
 * <p>Runs as an {@link ApplicationRunner} within a {@link TransactionTemplate} to ensure all
 * operations complete within a proper transaction.
 */
@Component
public class SampleDataInitializer implements ApplicationRunner {

  private final ProductCatalogService productCatalogService;
  private final PricingService pricingService;
  private final InventoryService inventoryService;
  private final TransactionTemplate transactionTemplate;

  public SampleDataInitializer(
      final ProductCatalogService productCatalogService,
      final PricingService pricingService,
      final InventoryService inventoryService,
      final TransactionTemplate transactionTemplate) {
    this.productCatalogService = productCatalogService;
    this.pricingService = pricingService;
    this.inventoryService = inventoryService;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public void run(final ApplicationArguments args) {
    transactionTemplate.executeWithoutResult(status -> loadSampleProducts());
  }

  private void loadSampleProducts() {
    // Electronics
    createProduct(
        "LAPTOP-001",
        "Professional Laptop",
        "Unleash your productivity with this high-performance laptop featuring 16GB RAM, a blazing-fast 512GB SSD, and a stunning 15.6-inch Retina display. Built for professionals who demand power and portability, it delivers all-day battery life and whisper-quiet operation.",
        "/images/products/laptop.svg",
        1299.99,
        "Electronics",
        15);

    createProduct(
        "PHONE-001",
        "Smartphone Pro",
        "Capture every moment in breathtaking detail with our flagship smartphone. The triple-lens 108MP camera system, edge-to-edge AMOLED display, and 5G connectivity make this the ultimate mobile companion for work and play.",
        "/images/products/smartphone.svg",
        899.99,
        "Electronics",
        25);

    createProduct(
        "TABLET-001",
        "Tablet Air",
        "The perfect blend of power and portability. This ultra-lightweight tablet features a vibrant 11-inch display, Apple M2 chip, and supports stylus input for creative professionals. Ideal for sketching, note-taking, and streaming on the go.",
        "/images/products/tablet.svg",
        599.99,
        "Electronics",
        30);

    // Clothing
    createProduct(
        "SHIRT-001",
        "Cotton T-Shirt",
        "Made from 100% organic combed cotton, this premium t-shirt offers unmatched softness and breathability. Available in 12 vibrant colors with a modern relaxed fit that looks great whether you dress it up or keep it casual.",
        "/images/products/tshirt.svg",
        29.99,
        "Clothing",
        100);

    createProduct(
        "JEANS-001",
        "Classic Jeans",
        "Crafted from premium selvedge denim with a classic straight-leg fit that never goes out of style. Features reinforced stitching, copper rivets, and a comfortable mid-rise waist. These jeans only get better with age.",
        "/images/products/jeans.svg",
        79.99,
        "Clothing",
        50);

    // Books
    createProduct(
        "BOOK-001",
        "Domain-Driven Design",
        "The seminal work by Eric Evans that introduced the software industry to Domain-Driven Design. This essential guide teaches you how to tackle complexity in the heart of software by connecting implementation to an evolving model of the business domain.",
        "/images/products/ddd-book.svg",
        54.99,
        "Books",
        20);

    createProduct(
        "BOOK-002",
        "Clean Architecture",
        "Robert C. Martin's definitive guide to software structure and design. Learn the universal rules of software architecture that dramatically improve developer productivity throughout the life of any software system.",
        "/images/products/clean-architecture-book.svg",
        39.99,
        "Books",
        35);

    // Home & Garden
    createProduct(
        "CHAIR-001",
        "Ergonomic Office Chair",
        "Designed in collaboration with orthopedic specialists, this premium office chair features adjustable lumbar support, breathable mesh back, and a 4D armrest system. Work in comfort for hours with proper spinal alignment and pressure distribution.",
        "/images/products/office-chair.svg",
        299.99,
        "Home & Garden",
        12);

    createProduct(
        "DESK-001",
        "Standing Desk",
        "Transform your workspace with this electric height-adjustable standing desk. Smooth dual-motor system transitions between sitting and standing in seconds, with programmable memory presets. The spacious 60x30 inch bamboo surface provides plenty of room for dual monitors.",
        "/images/products/standing-desk.svg",
        499.99,
        "Home & Garden",
        8);

    // Sports
    createProduct(
        "YOGA-001",
        "Yoga Mat Premium",
        "Elevate your practice with this professional-grade yoga mat. The dual-layer design provides superior cushioning and a non-slip surface that grips better the more you sweat. Includes a cotton carrying strap and is made from eco-friendly, biodegradable natural rubber.",
        "/images/products/yoga-mat.svg",
        49.99,
        "Sports",
        40);

    createProduct(
        "DUMBBELL-001",
        "Adjustable Dumbbells Set",
        "Replace an entire rack of weights with one smart set. These space-saving adjustable dumbbells let you switch between 5kg and 25kg in seconds with a simple twist-lock mechanism. Perfect for home workouts with professional-grade cast iron construction.",
        "/images/products/dumbbells.svg",
        199.99,
        "Sports",
        18);

    System.out.println("Sample data initialized: 11 products with prices and stock levels");
  }

  private void createProduct(
      final String sku,
      final String name,
      final String description,
      final String imageUrl,
      final double price,
      final String category,
      final int initialStock) {

    CreatedProduct created =
        productCatalogService.createProduct(
            sku,
            name,
            description,
            imageUrl,
            BigDecimal.valueOf(price),
            "EUR",
            category,
            initialStock);

    ProductId productId = created.productId();

    // Set initial price via Pricing API
    pricingService.setInitialPrice(productId, Money.euro(BigDecimal.valueOf(price)));

    // Set initial stock via Inventory API
    inventoryService.setInitialStock(productId, initialStock);
  }
}
