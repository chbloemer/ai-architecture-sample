package de.sample.aiarchitecture

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

/**
 * ArchUnit tests for Package Cycle Detection.
 *
 * Ensures no circular dependencies exist between packages:
 * - Domain model packages (per bounded context)
 * - Application layer packages (per bounded context)
 * - Incoming adapter packages (per bounded context)
 * - Outgoing adapter packages (per bounded context)
 *
 * Circular dependencies lead to tight coupling and make refactoring difficult.
 *
 * Reference: Clean Architecture, Acyclic Dependencies Principle (ADP)
 */
class PackageCyclesArchUnitTest extends BaseArchUnitTest {

  def "Domain Packages must not have cyclic dependencies"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.(*).domain.model..")
      .should().beFreeOfCycles()
      .because("Domain model packages should have clear boundaries and no cycles (Acyclic Dependencies Principle)")
      .check(allClasses)
  }

  def "Application Layer must not have cyclic dependencies"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.(*).application..")
      .should().beFreeOfCycles()
      .because("Application services should have clear boundaries and no cycles")
      .check(allClasses)
  }

  def "Outgoing Adapter Packages must not have cyclic dependencies"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.(*).adapter.outgoing..")
      .should().beFreeOfCycles()
      .because("Outgoing adapters should have clear boundaries and no cycles")
      .check(allClasses)
  }

  def "Incoming Adapter Packages must not have cyclic dependencies"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.(*).adapter.incoming..")
      .should().beFreeOfCycles()
      .because("Incoming adapters should have clear boundaries and no cycles")
      .check(allClasses)
  }
}
