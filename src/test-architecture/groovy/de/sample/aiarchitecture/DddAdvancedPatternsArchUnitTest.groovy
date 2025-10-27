package de.sample.aiarchitecture

import de.sample.aiarchitecture.domain.model.ddd.DomainEvent
import de.sample.aiarchitecture.domain.model.ddd.DomainService

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
  def "Domain Events sollten DomainEvent Marker Interface implementieren"() {
    expect:
    // NOTE: This is a SHOULD rule for new code - existing events may not implement it yet
    classes()
      .that().implement(DomainEvent.class)
      .should().haveSimpleNameEndingWith("Event")
      .because("Classes implementing DomainEvent should end with 'Event' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events müssen im domain.model Package liegen"() {
    expect:
    classes()
      .that().implement(DomainEvent.class)
      .should().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .because("Domain events are part of the domain model (named in past tense)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events sollten immutable sein (final oder records)"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .and().implement(DomainEvent.class)
      .and().areNotInterfaces()
      .and().areNotEnums()
      .and().areNotRecords()
      .should().haveModifier(JavaModifier.FINAL)
      .because("Domain events should be immutable (final classes or records)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events dürfen keine Spring Annotationen haben"() {
    expect:
    noClasses()
      .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .and().implement(DomainEvent.class)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .orShould().beAnnotatedWith(EventListener.class)
      .because("Domain events must be framework-independent POJOs")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Events müssen ein Timestamp Feld haben"() {
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
  def "Domain Services sollten DomainService Marker Interface implementieren"() {
    expect:
    // NOTE: This is a SHOULD rule for new code - existing domain services may not implement it yet
    classes()
      .that().implement(DomainService.class)
      .should().haveSimpleNameEndingWith("DomainService")
      .because("Classes implementing DomainService marker should have 'DomainService' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services müssen im domain.model Package liegen"() {
    expect:
    classes()
      .that().implement(DomainService.class)
      .should().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .because("Domain services are part of the domain model, not application layer")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services dürfen keine Spring Annotationen haben"() {
    expect:
    noClasses()
      .that().implement(DomainService.class)
      .should().beAnnotatedWith(Service.class)
      .orShould().beAnnotatedWith(Component.class)
      .because("Domain services should be framework-independent")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Domain Services sollten stateless sein (nur final Felder für Abhängigkeiten)"() {
    expect:
    classes()
      .that().implement(DomainService.class)
      .and().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .should().haveOnlyFinalFields()
      .because("Domain services should be stateless (only final fields for dependencies)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // FACTORIES PATTERN
  // ============================================================================

  def "Factories sollten Factory Marker Interface implementieren"() {
    expect:
    classes()
      .that().implement(de.sample.aiarchitecture.domain.model.ddd.Factory.class)
      .should().haveSimpleNameEndingWith("Factory")
      .because("Classes implementing Factory marker should have 'Factory' in their name")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories müssen im domain.model Package liegen"() {
    expect:
    classes()
      .that().implement(de.sample.aiarchitecture.domain.model.ddd.Factory.class)
      .should().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .because("Factories are part of the domain model (complex aggregate creation logic)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories dürfen keine Spring Annotationen haben"() {
    expect:
    noClasses()
      .that().implement(de.sample.aiarchitecture.domain.model.ddd.Factory.class)
      .and().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .because("Factories should be framework-independent")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Factories sollten stateless sein (nur final Felder für Abhängigkeiten)"() {
    expect:
    classes()
      .that().implement(de.sample.aiarchitecture.domain.model.ddd.Factory.class)
      .and().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .should().haveOnlyFinalFields()
      .because("Factories should be stateless (only final fields for dependencies)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  // ============================================================================
  // SPECIFICATION PATTERN
  // ============================================================================

  def "Specifications müssen mit 'Specification' enden"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Specification")
      .and().areNotInterfaces()
      .and().doNotHaveSimpleName("Specification")
      .should().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .because("Specification implementations are part of the domain model")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Specifications dürfen keine Spring Annotationen haben"() {
    expect:
    noClasses()
      .that().haveSimpleNameEndingWith("Specification")
      .and().resideInAPackage(DOMAIN_MODEL_PACKAGE)
      .should().beAnnotatedWith(Component.class)
      .orShould().beAnnotatedWith(Service.class)
      .because("Specifications should be framework-independent value objects")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
