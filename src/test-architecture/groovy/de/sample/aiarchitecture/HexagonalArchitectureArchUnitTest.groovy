package de.sample.aiarchitecture

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

/**
 * ArchUnit tests for Hexagonal Architecture (Ports and Adapters) pattern.
 *
 * Ensures:
 * - Clear separation between ports (interfaces) and adapters (implementations)
 * - Primary adapters (driving) handle incoming requests
 * - Secondary adapters (driven) handle outgoing integrations
 * - Adapters don't communicate directly with each other
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

  def "Port adapters (incoming and outgoing) must not communicate directly with each other"() {
    expect:
    noClasses()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .because("Port adapters should communicate through application services, not directly")
      .check(allClasses)

    noClasses()
      .that().resideInAPackage(OUTGOING_ADAPTER_PACKAGE)
      .should().dependOnClassesThat().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .because("Port adapters should communicate through application services, not directly")
      .check(allClasses)
  }

  def "Incoming adapters must only access their own bounded context (except event consumers)"() {
    expect:
    // Product incoming adapters (except event consumers) must not access other contexts
    noClasses()
      .that().resideInAPackage("${BASE_PACKAGE}.product.adapter.incoming..")
        .and().resideOutsideOfPackage("..adapter.incoming.event..")
      .should().accessClassesThat().resideInAnyPackage(
        CART_CONTEXT_PACKAGE,
        CHECKOUT_CONTEXT_PACKAGE,
        ACCOUNT_CONTEXT_PACKAGE,
        PORTAL_CONTEXT_PACKAGE
      )
      .because("Incoming adapters must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
      .check(allClasses)

    // Cart incoming adapters (except event consumers) must not access other contexts
    noClasses()
      .that().resideInAPackage("${BASE_PACKAGE}.cart.adapter.incoming..")
        .and().resideOutsideOfPackage("..adapter.incoming.event..")
      .should().accessClassesThat().resideInAnyPackage(
        PRODUCT_CONTEXT_PACKAGE,
        CHECKOUT_CONTEXT_PACKAGE,
        ACCOUNT_CONTEXT_PACKAGE,
        PORTAL_CONTEXT_PACKAGE
      )
      .because("Incoming adapters must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
      .check(allClasses)

    // Checkout incoming adapters (except event consumers) must not access other contexts
    noClasses()
      .that().resideInAPackage("${BASE_PACKAGE}.checkout.adapter.incoming..")
        .and().resideOutsideOfPackage("..adapter.incoming.event..")
      .should().accessClassesThat().resideInAnyPackage(
        PRODUCT_CONTEXT_PACKAGE,
        CART_CONTEXT_PACKAGE,
        ACCOUNT_CONTEXT_PACKAGE,
        PORTAL_CONTEXT_PACKAGE
      )
      .because("Incoming adapters must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
      .check(allClasses)

    // Account incoming adapters (except event consumers) must not access other contexts
    noClasses()
      .that().resideInAPackage("${BASE_PACKAGE}.account.adapter.incoming..")
        .and().resideOutsideOfPackage("..adapter.incoming.event..")
      .should().accessClassesThat().resideInAnyPackage(
        PRODUCT_CONTEXT_PACKAGE,
        CART_CONTEXT_PACKAGE,
        CHECKOUT_CONTEXT_PACKAGE,
        PORTAL_CONTEXT_PACKAGE
      )
      .because("Incoming adapters must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
      .check(allClasses)

    // Portal incoming adapters (except event consumers) must not access other contexts
    noClasses()
      .that().resideInAPackage("${BASE_PACKAGE}.portal.adapter.incoming..")
        .and().resideOutsideOfPackage("..adapter.incoming.event..")
      .should().accessClassesThat().resideInAnyPackage(
        PRODUCT_CONTEXT_PACKAGE,
        CART_CONTEXT_PACKAGE,
        CHECKOUT_CONTEXT_PACKAGE,
        ACCOUNT_CONTEXT_PACKAGE
      )
      .because("Incoming adapters must only orchestrate use cases from their own bounded context - use domain events for cross-context integration")
      .check(allClasses)
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
