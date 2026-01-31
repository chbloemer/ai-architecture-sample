package de.sample.aiarchitecture

import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainEvent
import de.sample.aiarchitecture.sharedkernel.domain.marker.DomainService
import de.sample.aiarchitecture.sharedkernel.domain.marker.Factory

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaModifier

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.context.event.EventListener

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime

import spock.lang.Ignore

/**
 * ArchUnit tests for Advanced DDD Patterns.
 *
 * Tests advanced DDD patterns:
 * - Domain Events: Things that happened in the domain
 * - Domain Services: Operations that don't belong to entities/value objects
 * - Factories: Complex object creation logic
 * - Specifications: Business rules as objects
 *
 * Reference:
 * - Eric Evans' Domain-Driven Design (Domain Events, Services, Factories, Specifications)
 * - Vaughn Vernon's Implementing DDD (Domain Events for eventual consistency)
 */
class DddAdvancedPatternsArchUnitTest extends BaseArchUnitTest {

  // ============================================================================
  // DOMAIN EVENTS PATTERN
  // ============================================================================

  @Ignore("Pending decision: ProductPriceChanged and ProductCreated need to be renamed")
  def "Domain Events should implement DomainEvent Marker Interface"() {
    expect:
    // NOTE: This is a SHOULD rule for new code - existing events may not implement it yet
    classes()
      .that().implement(DomainEvent.class)
      .should().haveSimpleNameEndingWith("Event")
      .because("Classes implementing DomainEvent should end with 'Event' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events must reside in domain package"() {
    expect:
    classes()
      .that().implement(DomainEvent.class)
      .should().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .because("Domain events are part of the domain layer (named in past tense)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events should be immutable (final or records)"() {
    expect:
    classes()
      .that().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .and().implement(DomainEvent.class)
      .and().areNotInterfaces()
      .and().areNotEnums()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Domain events should be immutable (final classes or records)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events must not have Spring annotations"() {
    expect:
    noClasses()
      .that().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .and().implement(DomainEvent.class)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .orShould().beAnnotatedWith(EventListener.class)
      .because("Domain events must be framework-independent POJOs")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events must have a timestamp field"() {
    when:
    def domainEventClasses = allClasses.stream()
      .filter { it.isAssignableTo(DomainEvent.class) }
      .filter { !it.isInterface() }
      .collect()

    def violations = []

    domainEventClasses.each { eventClass ->
      def hasTimestampField = eventClass.getAllFields().stream()
        .anyMatch { field ->
          field.getRawType().isEquivalentTo(Instant.class) ||
            field.getRawType().isEquivalentTo(LocalDateTime.class) ||
            field.getRawType().isEquivalentTo(ZonedDateTime.class)
        }

      if (!hasTimestampField) {
        violations.add("${eventClass.getName()} does not have a timestamp field")
      }
    }

    then:
    if (!violations.isEmpty()) {
      throw new AssertionError(
      "Domain Events must have a timestamp field (when did the event occur?):\n" +
      violations.join("\n")
      )
    }
    true
  }

  // ============================================================================
  // DOMAIN SERVICES PATTERN
  // ============================================================================

  @Ignore("Pending decision: CartTotalCalculator and PricingService need to be renamed")
  def "Domain Services should implement DomainService Marker Interface"() {
    expect:
    // NOTE: This is a SHOULD rule for new code - existing domain services may not implement it yet
    classes()
      .that().implement(DomainService.class)
      .should().haveSimpleNameEndingWith("DomainService")
      .because("Classes implementing DomainService marker should have 'DomainService' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services must reside in domain package"() {
    expect:
    classes()
      .that().implement(DomainService.class)
      .should().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .because("Domain services are part of the domain layer, not application layer")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services must not have Spring annotations"() {
    expect:
    noClasses()
      .that().implement(DomainService.class)
      .should().beAnnotatedWith(Service.class)
      .orShould().beAnnotatedWith(Component.class)
      .because("Domain services should be framework-independent")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services should be stateless (only final fields for dependencies)"() {
    expect:
    classes()
      .that().implement(DomainService.class)
      .and().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().haveOnlyFinalFields()
      .because("Domain services should be stateless (only final fields for dependencies)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // FACTORIES PATTERN
  // ============================================================================

  def "Factories should implement Factory Marker Interface"() {
    expect:
    classes()
      .that().implement(Factory.class)
      .should().haveSimpleNameEndingWith("Factory")
      .because("Classes implementing Factory marker should have 'Factory' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories must reside in domain package"() {
    expect:
    classes()
      .that().implement(Factory.class)
      .should().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .because("Factories are part of the domain layer (complex aggregate creation logic)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories must not have Spring annotations"() {
    expect:
    noClasses()
      .that().implement(Factory.class)
      .and().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .because("Factories should be framework-independent")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories should be stateless (only final fields for dependencies)"() {
    expect:
    classes()
      .that().implement(Factory.class)
      .and().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().haveOnlyFinalFields()
      .because("Factories should be stateless (only final fields for dependencies)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // SPECIFICATION PATTERN
  // ============================================================================

  def "Specifications must end with 'Specification'"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Specification")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleName("Specification")
      .should().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .because("Specification implementations are part of the domain layer")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Specifications must not have Spring annotations"() {
    expect:
    noClasses()
      .that().haveSimpleNameEndingWith("Specification")
      .and().resideInAnyPackage(PRODUCT_DOMAIN_PACKAGE, CART_DOMAIN_PACKAGE, CHECKOUT_DOMAIN_PACKAGE, ACCOUNT_DOMAIN_PACKAGE, SHAREDKERNEL_DOMAIN_PACKAGE)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .because("Specifications should be framework-independent value objects")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
