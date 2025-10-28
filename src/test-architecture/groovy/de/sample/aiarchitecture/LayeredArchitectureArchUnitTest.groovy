package de.sample.aiarchitecture

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
    layeredArchitecture()
      .consideringAllDependencies()
      .layer("Infrastructure").definedBy(INFRASTRUCTURE_PACKAGE)
      .layer("ApplicationServices").definedBy(APPLICATION_PACKAGE)
      .layer("IncomingAdapters").definedBy(INCOMING_ADAPTER_PACKAGE)
      .layer("OutgoingAdapters").definedBy(OUTGOING_ADAPTER_PACKAGE)
      .whereLayer("OutgoingAdapters").mayNotBeAccessedByAnyLayer()
      .whereLayer("IncomingAdapters").mayNotBeAccessedByAnyLayer()
      .whereLayer("ApplicationServices").mayOnlyBeAccessedByLayers("IncomingAdapters")
      .because("outgoing adapters should not actively call any application services (business use cases)" +
      " - they are merely used to write/send data")
      .check(allClasses)
  }

  def "Domain must not have dependencies on Infrastructure"() {
    expect:
    noClasses()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
      .because("Domain should not depend on infrastructure concerns (Dependency Inversion Principle)")
      .check(allClasses)
  }

  def "Application Services must only use infrastructure.api (not infrastructure implementations)"() {
    expect:
    noClasses()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .should().dependOnClassesThat(INFRASTRUCTURE_IMPLEMENTATION)
      .because("Application services should only use infrastructure.api (public SPI), not infrastructure implementation details")
      .check(allClasses)
  }

  def "infrastructure.api should only contain interfaces (Service Provider Interface)"() {
    expect:
    classes()
      .that().resideInAPackage(INFRASTRUCTURE_API_PACKAGE)
      .should().beInterfaces()
      .because("infrastructure.api is the Service Provider Interface (SPI) and should only contain abstractions/contracts, " +
      "not concrete implementations. This ensures the application layer remains framework-independent and " +
      "follows the Dependency Inversion Principle. Implementations belong in infrastructure.config or other infrastructure packages.")
      .check(allClasses)
  }
}
