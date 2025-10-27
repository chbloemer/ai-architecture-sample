package de.sample.aiarchitecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * ArchUnit tests for DDD Strategic Patterns.
 *
 * Tests strategic DDD concepts:
 * - Bounded Contexts: Explicit boundaries with linguistic consistency
 * - Context Mapping: Relationships between bounded contexts
 * - Shared Kernel: Small, carefully curated shared domain model
 * - Context Isolation: Bounded contexts should not directly depend on each other
 *
 * Identified Bounded Contexts in this service:
 * 1. Shared Kernel (shared) - Shared value objects across contexts
 *    - Money, ProductId, Price
 * 2. Product Catalog Context (product) - Product management
 * 3. Shopping Cart Context (cart) - Cart and checkout operations
 *
 * Context Mapping Strategy:
 * - Shared Kernel Pattern: Common value objects (Money, ProductId, Price)
 * - Both contexts may access the Shared Kernel
 * - Contexts may NOT access each other directly
 * - Aggregates reference other aggregates by ID only (through Shared Kernel)
 *
 * Reference:
 * - Eric Evans' Domain-Driven Design (Strategic Design section, Chapter 14)
 * - Vaughn Vernon's Implementing DDD (Chapter 2: Domains, Subdomains, and Bounded Contexts)
 * - Martin Fowler's patterns of Enterprise Application Architecture
 */
class DddStrategicPatternsArchUnitTest extends BaseArchUnitTest {

  // ============================================================================
  // SHARED KERNEL PATTERN
  // ============================================================================

  def "Shared Kernel darf keine Abhängigkeiten auf bounded contexts haben"() {
    expect:
    // The Shared Kernel should be truly shared - no dependencies on specific contexts
    noClasses()
      .that().resideInAPackage(SHARED_KERNEL_PACKAGE)
      .should().accessClassesThat().resideInAPackage(PRODUCT_CONTEXT_PACKAGE)
      .orShould().accessClassesThat().resideInAPackage(CART_CONTEXT_PACKAGE)
      .because("Shared Kernel must be context-independent to be truly shared (DDD Strategic Pattern)")
      .check(allClasses)
  }

  // ============================================================================
  // BOUNDED CONTEXT ISOLATION
  // ============================================================================

  def "Product Context darf Cart Context nicht direkt zugreifen"() {
    expect:
    // Product context should not access Cart context directly
    // Integration must happen through Shared Kernel or domain events
    noClasses()
      .that().resideInAPackage(PRODUCT_CONTEXT_PACKAGE)
      .should().accessClassesThat().resideInAPackage(CART_CONTEXT_PACKAGE)
      .because("Bounded contexts must remain isolated - use Shared Kernel or events for integration")
      .check(allClasses)
  }

  def "Cart Context darf Product Context nicht direkt zugreifen"() {
    expect:
    // Cart context should not access Product context directly
    // Cart can reference Product by ProductId (from Shared Kernel), but not the Product aggregate itself
    noClasses()
      .that().resideInAPackage(CART_CONTEXT_PACKAGE)
      .should().accessClassesThat().resideInAPackage(PRODUCT_CONTEXT_PACKAGE)
      .because("Bounded contexts must remain isolated - Cart references Product by ID only (through Shared Kernel)")
      .check(allClasses)
  }

  // Note: Shared Kernel Pattern (Eric Evans, DDD Chapter 14)
  //
  // In this architecture, we use the Shared Kernel pattern for cross-cutting value objects:
  //
  // Shared Kernel (domain.model.shared):
  // - Money: Universal monetary value representation
  // - ProductId: Product identifier shared across contexts
  // - Price: Pricing value object wrapping Money
  //
  // Benefits:
  // - Consistency: Single definition of Money prevents currency mismatches
  // - Reduced duplication: Avoid reimplementing common concepts
  // - Clear boundaries: Explicit shared vs. context-specific
  //
  // Trade-offs:
  // - Coupling: Changes to shared kernel affect all contexts (requires coordination)
  // - Limited scope: Only truly universal concepts belong here
  //
  // What belongs in Shared Kernel:
  // ✅ Universal value objects (Money, Currency)
  // ✅ Cross-context identifiers (ProductId when used by multiple contexts)
  // ✅ Common domain primitives
  //
  // What does NOT belong in Shared Kernel:
  // ❌ Aggregates (each belongs to one context)
  // ❌ Context-specific business logic
  // ❌ Infrastructure concerns
  //
  // Alternative patterns considered:
  // - Separate Ways: Duplicate Money in each context (rejected: high risk of inconsistency)
  // - Published Language: Share via events (rejected: too complex for simple value objects)
  // - Customer/Supplier: One context defines, other consumes (rejected: creates ownership issues)
}
