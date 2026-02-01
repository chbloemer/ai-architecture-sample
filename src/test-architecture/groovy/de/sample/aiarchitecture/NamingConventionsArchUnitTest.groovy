package de.sample.aiarchitecture

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

import de.sample.aiarchitecture.sharedkernel.marker.port.in.UseCase
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

  def "Application layer InputPort implementations must end with 'UseCase'"() {
    expect:
    classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .and().areNotInterfaces()
      .and().areNotRecords()
      .and().implement(UseCase.class)
      .should().haveSimpleNameEndingWith("UseCase")
      .because("InputPort implementations (use cases) should follow consistent naming conventions (Hexagonal Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Use case classes must be annotated with @Service"() {
    expect:
    classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .and().haveSimpleNameEndingWith("UseCase")
      .and().areNotInterfaces()  // Exclude the UseCase interface itself
      .should().beAnnotatedWith(Service.class)
      .because("Use case classes must be Spring-managed beans")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "InputPort interfaces must end with 'InputPort'"() {
    expect:
    classes()
      .that().resideInAPackage("..application.port.in..")
      .and().areInterfaces()
      .should().haveSimpleNameEndingWith("InputPort")
      .because("Input port interfaces should follow consistent naming conventions (Hexagonal Architecture)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Repository Interfaces must end with 'Repository'"() {
    expect:
    classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)  // Repositories are now in application.shared
      .and().areInterfaces()
      .and().haveSimpleNameContaining("Repository")
      .and().doNotHaveSimpleName("Repository")  // Exclude the base Repository interface
      .should().haveSimpleNameEndingWith("Repository")
      .because("Repository interfaces should follow consistent naming conventions (DDD pattern)")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Controller classes must end with 'Controller'"() {
    expect:
    classes()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .and().areAnnotatedWith(Controller.class)
      .should().haveSimpleNameEndingWith("Controller")
      .because("@Controller annotated classes should follow naming conventions")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "REST Controllers must end with 'Resource' (REST best practice)"() {
    expect:
    classes()
      .that().resideInAPackage(INCOMING_ADAPTER_PACKAGE)
      .and().areAnnotatedWith(RestController.class)
      .should().haveSimpleNameEndingWith("Resource")
      .because("@RestController annotated classes should end with 'Resource' following RESTful naming conventions")
      .check(allClasses)
  }

  def "DTOs must reside in portadapter package (not in domain or application)"() {
    expect:
    classes()
      .that().haveSimpleNameEndingWith("Dto")
      .and().resideInAnyPackage(BASE_PACKAGE + "..")
      .should().resideInAPackage(PORTADAPTER_PACKAGE)
      .because("DTOs are adapter concerns (presentation or external API) - not in domain or application")
      .allowEmptyShould(true)
      .check(allClasses)
  }

  def "Converters must reside in portadapter package"() {
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
