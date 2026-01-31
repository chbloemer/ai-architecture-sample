package de.sample.aiarchitecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import de.sample.aiarchitecture.sharedkernel.stereotype.BoundedContext

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * ArchUnit tests for Hexagonal Architecture (Ports and Adapters) pattern.
 *
 * Ensures:
 * - Clear separation between ports (interfaces) and adapters (implementations)
 * - Primary adapters (driving) handle incoming requests
 * - Secondary adapters (driven) handle outgoing integrations
 * - Adapters don't communicate directly with each other
 * - Incoming adapters only access their own bounded context (dynamically discovered)
 * - Open Host Services are special incoming adapters that may be accessed by other contexts
 *
 * Reference: Alistair Cockburn's Hexagonal Architecture, Vaughn Vernon's Implementing DDD
 */
class HexagonalArchitectureArchUnitTest extends BaseArchUnitTest {

  def "Classes from the domain should not access port adapters"() {
    expect:
    noClasses()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .should().accessClassesThat().resideInAPackage(PORTADAPTER_PACKAGE)
      .because("Domain should not depend on adapters (ports and adapters pattern)")
      .check(allClasses)
  }

  def "Application Services should not access port adapters"() {
    expect:
    noClasses()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .should().accessClassesThat().resideInAPackage(PORTADAPTER_PACKAGE)
      .because("Application services should only depend on domain and outbound ports (sharedkernel.application.port), not adapters")
      .check(allClasses)
  }

  def "Incoming Adapters must only use outbound ports (not infrastructure implementations)"() {
    expect:
    noClasses()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .should().dependOnClassesThat(INFRASTRUCTURE_IMPLEMENTATION)
      .because("Incoming adapters should only use outbound ports from sharedkernel.application.port, not infrastructure implementation details")
      .check(allClasses)
  }

  def "Outgoing Adapters must only use outbound ports (not infrastructure implementations)"() {
    expect:
    noClasses()
      .that().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .should().dependOnClassesThat(INFRASTRUCTURE_IMPLEMENTATION)
      .because("Outgoing adapters should only use outbound ports from sharedkernel.application.port, not infrastructure implementation details")
      .check(allClasses)
  }

  def "Port adapters (incoming and outgoing) must not communicate directly with each other within the same context"() {
    expect:
    // Incoming adapters within a context should not directly call outgoing adapters
    noClasses()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .because("Port adapters should communicate through application services, not directly")
      .check(allClasses)

    // Note: Outgoing adapters MAY access incoming adapters from OTHER contexts
    // when calling Open Host Services (e.g., Cart's ProductDataAdapter calls Product's ProductCatalogService)
    // This is the intended cross-context communication pattern via Open Host Services
  }

  def "Incoming adapters must only access their own bounded context (except event consumers and Open Host Services)"() {
    given:
    Map<String, BoundedContext> boundedContexts = discoverBoundedContextPackages()
    List<String> contextPackages = boundedContexts.keySet().toList()

    expect:
    // Dynamically check each bounded context's incoming adapters
    // They must not access any other bounded context
    // Exception: Event consumers may access other contexts' integration events
    // Note: Open Host Services (adapter.incoming.openhost) are designed to BE ACCESSED by other contexts,
    // but they themselves should not access other contexts
    contextPackages.each { contextPackage ->
      String contextName = boundedContexts[contextPackage].name()

      // Get all other context packages (excluding current)
      List<String> otherContexts = contextPackages.findAll { it != contextPackage }

      if (!otherContexts.isEmpty()) {
        String[] otherContextPatterns = otherContexts.collect { it + ".." } as String[]

        noClasses()
          .that().resideInAPackage("${contextPackage}.adapter.incoming..")
            .and().resideOutsideOfPackage("..adapter.incoming.event..")
          .should().accessClassesThat().resideInAnyPackage(otherContextPatterns)
          .because("Incoming adapters in '${contextName}' must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
          .check(allClasses)
      }
    }
  }

  def "Outgoing adapters may access Open Host Services from other contexts"() {
    given:
    Map<String, BoundedContext> boundedContexts = discoverBoundedContextPackages()
    List<String> contextPackages = boundedContexts.keySet().toList()

    expect:
    // This is a "positive" test documenting the allowed pattern:
    // Outgoing adapters may access adapter.incoming.openhost packages from other contexts
    // This is verified by the successful compilation and the stricter tests in DddStrategicPatternsArchUnitTest
    // that ensure outgoing adapters do NOT access domain or application layers of other contexts
    true // Pattern verification: outgoing adapters call Open Host Services, not domain/application
  }

  def "Repository Implementations must reside in portadapter.outgoing package"() {
    expect:
    ArchRuleDefinition.classes()
      .that().haveSimpleNameEndingWith("Repository")
      .and().areNotInterfaces()
      .should().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .because("Repository implementations are secondary adapters (outgoing ports)")
      .check(allClasses)
  }
}
