package de.sample.aiarchitecture

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

  def "Die Regeln der Layered Architektur sollten eingehalten werden"() {
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

  def "Domain darf keine Abhängigkeiten auf Infrastructure haben"() {
    expect:
    noClasses()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
      .because("Domain should not depend on infrastructure concerns (Dependency Inversion Principle)")
      .check(allClasses)
  }

  def "Application Services dürfen nur infrastructure.api verwenden (nicht infrastructure Implementierungen)"() {
    expect:
    noClasses()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .should().dependOnClassesThat(INFRASTRUCTURE_IMPLEMENTATION)
      .because("Application services should only use infrastructure.api (public SPI), not infrastructure implementation details")
      .check(allClasses)
  }
}
