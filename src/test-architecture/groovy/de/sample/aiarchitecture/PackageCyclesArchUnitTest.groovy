package de.sample.aiarchitecture

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

/**
 * ArchUnit tests for Package Cycle Detection.
 *
 * Ensures no circular dependencies exist between packages:
 * - Domain model packages
 * - Application layer packages
 * - Primary adapter packages
 * - Secondary adapter packages
 *
 * Circular dependencies lead to tight coupling and make refactoring difficult.
 *
 * Reference: Clean Architecture, Acyclic Dependencies Principle (ADP)
 */
class PackageCyclesArchUnitTest extends BaseArchUnitTest {

  def "Domain Packages dürfen keine zyklischen Abhängigkeiten haben"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.domain.model.(*)..")
      .should().beFreeOfCycles()
      .allowEmptyShould(true)
      .because("Domain model packages should have clear boundaries and no cycles (Acyclic Dependencies Principle)")
      .check(allClasses)
  }

  def "Application Layer darf keine zyklischen Abhängigkeiten haben"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.application.(*)..")
      .should().beFreeOfCycles()
      .allowEmptyShould(true)
      .because("Application services should have clear boundaries and no cycles")
      .check(allClasses)
  }

  def "Secondary Adapter Packages dürfen keine zyklischen Abhängigkeiten haben"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.portadapter.secondary.(*)..")
      .should().beFreeOfCycles()
      .allowEmptyShould(true)
      .because("Secondary adapters should have clear boundaries and no cycles")
      .check(allClasses)
  }

  def "Primary Adapter Packages dürfen keine zyklischen Abhängigkeiten haben"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.portadapter.primary.(*)..")
      .should().beFreeOfCycles()
      .allowEmptyShould(true)
      .because("Primary adapters should have clear boundaries and no cycles")
      .check(allClasses)
  }

  def "Primary Web Sub-Packages dürfen keine zyklischen Abhängigkeiten haben"() {
    expect:
    slices()
      .matching("${BASE_PACKAGE}.portadapter.primary.web.(*)..")
      .should().beFreeOfCycles()
      .allowEmptyShould(true)
      .because("Web adapter sub-packages should have clear boundaries and no cycles")
      .check(allClasses)
  }
}
