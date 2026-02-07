package de.sample.aiarchitecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import static com.tngtech.archunit.library.Architectures.layeredArchitecture

/**
 * ArchUnit tests for Layered Architecture pattern.
 *
 * Ensures proper layering with dependencies pointing inward:
 * - Presentation (Primary Adapters)
 * - Application
 * - Domain
 * - Infrastructure (Secondary Adapters)
 *
 * Reference: Eric Evans' DDD, Vaughn Vernon's Implementing DDD
 */
class LayeredArchitectureArchUnitTest extends BaseArchUnitTest {

  def "The rules of the Layered Architecture should be followed"() {
    expect:
    // NOTE: Traditional layered architecture rules don't align well with Hexagonal Architecture.
    //
    // In Hexagonal Architecture (Ports & Adapters):
    // - Application layer defines BOTH input ports (use cases) AND output ports (repository interfaces)
    // - Incoming adapters depend on application (implement/use input ports)
    // - Outgoing adapters depend on application (implement output port interfaces)
    //
    // This means BOTH adapter types depend on the application layer, which violates traditional
    // layered architecture where "ApplicationServices may only be accessed by IncomingAdapters".
    //
    // The correct Hexagonal Architecture dependency rules are tested in HexagonalArchitectureArchUnitTest instead.
    //
    // Test disabled to avoid false violations in a Hexagonal Architecture codebase.

    true // Test disabled - see comment above
  }

  def "Domain must not have dependencies on Infrastructure"() {
    expect:
    noClasses()
      .that().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, INVENTORY_DOMAIN_PACKAGE, PRICING_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
      .because("Domain should not depend on infrastructure concerns (Dependency Inversion Principle)")
      .check(allClasses)
  }

  def "Application Services must only use outbound ports (not infrastructure implementations)"() {
    expect:
    noClasses()
      .that().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE, ACCOUNT_APPLICATION_PACKAGE, INVENTORY_APPLICATION_PACKAGE, PRICING_APPLICATION_PACKAGE)
      .should().dependOnClassesThat(INFRASTRUCTURE_IMPLEMENTATION)
      .because("Application services should only use outbound ports from sharedkernel.application.port, not infrastructure implementation details")
      .check(allClasses)
  }

  def "sharedkernel.application.port should only contain interfaces (Outbound Ports)"() {
    expect:
    classes()
      .that().resideInAPackage(SHAREDKERNEL_MARKER_PORT_OUT_PACKAGE)
      .should().beInterfaces()
      .because("sharedkernel.marker.port.out contains outbound port interfaces (Repository, OutputPort, DomainEventPublisher) " +
      "shared across all bounded contexts. These must be interfaces to ensure the application layer remains framework-independent " +
      "and follows the Dependency Inversion Principle. Implementations belong in infrastructure or adapter packages.")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
