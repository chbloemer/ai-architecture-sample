package de.sample.aiarchitecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

import org.springframework.stereotype.Service
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController

/**
 * ArchUnit tests for Naming Conventions.
 *
 * Ensures consistent naming across the codebase:
 * - Application Services
 * - Repositories
 * - Controllers
 * - DTOs and Converters
 * - Domain patterns (Events, Services, Factories, Specifications)
 *
 * Reference: Ubiquitous Language from DDD, coding standards
 */
class NamingConventionsArchUnitTest extends BaseArchUnitTest {

  def "Application Services müssen mit 'ApplicationService' enden"() {
    expect:
    classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .should().haveSimpleNameEndingWith("ApplicationService")
      .because("Application services should follow consistent naming conventions")
      .check(allClasses)
  }

  def "Application Services müssen @Service annotiert sein"() {
    expect:
    classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .and().haveSimpleNameEndingWith("ApplicationService")
      .should().beAnnotatedWith(Service.class)
      .because("Application services must be Spring-managed beans")
      .check(allClasses)
  }

  def "Repository Interfaces müssen mit 'Repository' enden"() {
    expect:
    classes()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .and().areInterfaces()
      .and().haveSimpleNameContaining("Repository")
      .should().haveSimpleNameEndingWith("Repository")
      .because("Repository interfaces should follow consistent naming conventions (DDD pattern)")
      .check(allClasses)
  }

  def "Controller Klassen müssen mit 'Controller' enden"() {
    expect:
    classes()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .and().areAnnotatedWith(Controller.class)
      .should().haveSimpleNameEndingWith("Controller")
      .because("@Controller annotated classes should follow naming conventions")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "REST Controllers müssen mit 'Resource' enden (REST best practice)"() {
    expect:
    classes()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .and().areAnnotatedWith(RestController.class)
      .should().haveSimpleNameEndingWith("Resource")
      .because("@RestController annotated classes should end with 'Resource' following RESTful naming conventions")
      .check(allClasses)
  }

  def "DTOs müssen im portadapter Package liegen (nicht in domain oder application)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Dto")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAPackage(PORTADAPTER_PACKAGE)
      .because("DTOs are adapter concerns (presentation or external API) - not in domain or application")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Konverter müssen im portadapter Package liegen"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Converter")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAPackage(PORTADAPTER_PACKAGE)
      .because("Converters/Mappers translate between layers and should be in adapters")
      .allowEmptyShould(true)
      .check(allClasses)
  }
}
