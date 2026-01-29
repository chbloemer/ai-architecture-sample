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

  def "Base InputPort interface must be in sharedkernel application package"() {
    expect:
    classes()
      .that().areInterfaces()
      .and().haveSimpleName("InputPort")
      .should().resideInAPackage(SHAREDKERNEL_APPLICATION_PACKAGE)
      .because("Base InputPort interface defines the generic contract for all use cases (Hexagonal Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // USE CASE COMMAND/QUERY MODEL PATTERN
  // ============================================================================

  def "Use Case Commands must end with 'Command' and reside in application package"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Command")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .because("Use case commands should be in application layer (CQRS pattern)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Queries must end with 'Query' and reside in application package"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Query")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .because("Use case queries should be in application layer (CQRS pattern)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Commands should be immutable (final or records)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Command")
      .and().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Use case commands should be immutable (value objects)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Queries should be immutable (final or records)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Query")
      .and().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Use case queries should be immutable (value objects)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // USE CASE RESPONSE MODEL PATTERN
  // ============================================================================

  def "Use Case Response Models must end with 'Response' and reside in application package"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Response")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .and().doNotHaveSimpleName("DomainEventPublisher") // Exclude infrastructure classes
      .should().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .because("Use case response models should be in application layer (Clean Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use Case Response Models should be immutable (final or records)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Response")
      .and().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Use case response models should be immutable (value objects)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // MAPPING STRATEGY (DTOs)
  // ============================================================================

  def "DTOs must not be used in the Domain Layer"() {
    expect:
    noClasses()
      .that().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().dependOnClassesThat().haveSimpleNameEndingWith("Dto")
      .because("Domain layer should not depend on DTOs (presentation concerns) - Dependency Inversion Principle")
      .check(allClasses)
  }

  def "DTOs must not be used in the Application Layer"() {
    expect:
    noClasses()
      .that().resideInAnyPackage(PRODUCT_APPLICATION_PACKAGE, CART_APPLICATION_PACKAGE, CHECKOUT_APPLICATION_PACKAGE)
      .should().dependOnClassesThat().haveSimpleNameEndingWith("Dto")
      .because("Application layer should use Command/Query/Response models, not presentation DTOs (Clean Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
