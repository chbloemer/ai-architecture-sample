package de.sample.aiarchitecture.domain.model.ddd;

/**
 * Marker interface for Domain Services.
 *
 * <p>Domain Services are stateless operations that don't naturally belong to an Entity or Value
 * Object. They encapsulate domain logic that involves multiple domain objects or doesn't fit within
 * a single Aggregate.
 *
 * <p><b>Characteristics:</b>
 *
 * <ul>
 *   <li>Stateless (only final fields for dependencies)
 *   <li>Named after activities or actions (e.g., PricingService, CartTotalCalculator)
 *   <li>Express domain concepts in the Ubiquitous Language
 *   <li>Should NOT have Spring annotations (@Service, @Component)
 *   <li>Instantiated by Application Services
 * </ul>
 *
 * <p><b>Examples:</b>
 *
 * <ul>
 *   <li>Calculating cart totals with complex tax rules (involves multiple items)
 *   <li>Applying pricing rules and discounts (domain logic not belonging to a single entity)
 *   <li>Validating business constraints that span multiple aggregates
 * </ul>
 *
 * <p><b>Reference:</b> Eric Evans' Domain-Driven Design (2003), Chapter 5: "A Model Expressed in
 * Software"
 *
 * @see <a href="https://www.domainlanguage.com/ddd/">Domain-Driven Design Reference</a>
 */
public interface DomainService {}
