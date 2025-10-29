package de.sample.aiarchitecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaModifier

/**
 * ArchUnit tests for Use Case and Mapping Patterns.
 *
 * Tests patterns for clean architecture use cases:
 * - Base UseCase interface (generic contract)
 * - Use Case implementations
 * - Use Case Input/Output models
 * - DTOs and mapping strategy
 *
 * These patterns ensure:
 * - Explicit use case contracts
 * - Decoupling from domain models
 * - Clear boundaries between layers
 *
 * Reference:
 * - "Get Your Hands Dirty on Clean Architecture" (Tom Hombergs)
 * - Robert C. Martin's Clean Architecture
 * - Hexagonal Architecture (input ports)
 */
class UseCasePatternsArchUnitTest extends BaseArchUnitTest {

  // ============================================================================
  // USE CASE INTERFACE PATTERN (Base Contract)
  // ============================================================================

  def "Base UseCase interface must be in application package"() {
    expect:
    classes()
      .that().areInterfaces()
      .and().haveSimpleName("UseCase")
      .should().resideInAPackage(APPLICATION_PACKAGE)
      .because("Base UseCase interface defines the generic contract for all use cases (Clean Architecture)")
      .check(allClasses)
  }

  // ============================================================================
  // USE CASE INPUT MODEL PATTERN
  // ============================================================================

  def "Use Case Input Models must end with 'Input' and reside in application package"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Input")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAPackage(APPLICATION_PACKAGE)
      .because("Use case input models should be in application layer (Clean Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Input Models should be immutable (final or records)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Input")
      .and().resideInAPackage(APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Use case input models should be immutable (value objects)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // USE CASE OUTPUT MODEL PATTERN
  // ============================================================================

  def "Use Case Output Models must end with 'Output' and reside in application package"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Output")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAPackage(APPLICATION_PACKAGE)
      .because("Use case output models should be in application layer (Clean Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Output Models should be immutable (final or records)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Output")
      .and().resideInAPackage(APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Use case output models should be immutable (value objects)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // MAPPING STRATEGY (DTOs)
  // ============================================================================

  def "DTOs must not be used in the Domain Layer"() {
    expect:
    noClasses()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .should().dependOnClassesThat().haveSimpleNameEndingWith("Dto")
      .because("Domain layer should not depend on DTOs (presentation concerns) - Dependency Inversion Principle")
      .check(allClasses)
  }

  def "DTOs must not be used in the Application Layer"() {
    expect:
    noClasses()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .should().dependOnClassesThat().haveSimpleNameEndingWith("Dto")
      .because("Application layer should use Input/Output models, not presentation DTOs (Clean Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
