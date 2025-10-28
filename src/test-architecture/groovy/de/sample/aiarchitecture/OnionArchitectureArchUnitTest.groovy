package de.sample.aiarchitecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule

import java.util.stream.Stream

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

/**
 * ArchUnit tests for Onion Architecture pattern.
 *
 * Ensures dependencies point inward toward the domain:
 * - Domain (innermost layer) depends on nothing
 * - Application depends only on Domain
 * - Infrastructure depends on Application and Domain
 * - Presentation depends on Application
 *
 * Reference: Jeffrey Palermo's Onion Architecture, Clean Architecture by Robert C. Martin
 */
class OnionArchitectureArchUnitTest extends BaseArchUnitTest {

  def "Domain must not access Application Services (Onion Architecture - Domain is innermost layer)"() {
    expect:
    noClasses()
    .that().resideInAPackage(DOMAIN_PACKAGE)
    .should().dependOnClassesThat().resideInAPackage(APPLICATION_PACKAGE)
    .because("Domain is the innermost layer in onion architecture and should not depend on application services")
    .check(allClasses)
  }

  def "The Domain Model should be framework independent and should not use 3rd party libraries when possible"() {
    expect:
    final JavaClasses importedClasses = new ClassFileImporter()
    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
    .importPackages(DOMAIN_PACKAGE)

    final String domainPackagePattern = "${BASE_PACKAGE}.domain.." as String

    final ArchRule domainClassesMustNotDependOnAnyFrameworkOr3rdParty =
    classes()
    .that().resideInAPackage("${BASE_PACKAGE}.domain.(**)")
    .should().onlyDependOnClassesThat().resideInAnyPackage(Stream.concat(
    THIRD_PARTY_PACKAGES_ALLOWED_IN_DOMAIN.stream(),
    Stream.of(domainPackagePattern)).toArray(String[]::new))
    .because("Domain should be framework-independent (Dependency Inversion Principle)")

    domainClassesMustNotDependOnAnyFrameworkOr3rdParty.check(importedClasses)
  }

  def "Domain Models must not have Spring/JPA annotations"() {
    expect:
    noClasses()
    .that().resideInAPackage(DOMAIN_MODEL_PACKAGE)
    .should().beAnnotatedWith(Component.class)
    .orShould().beAnnotatedWith(Service.class)
    .orShould().beAnnotatedWith("jakarta.persistence.Entity")
    .orShould().beAnnotatedWith("jakarta.persistence.Table")
    .because("Domain models must be framework-independent (no Spring or JPA annotations)")
    .check(allClasses)
  }
}
