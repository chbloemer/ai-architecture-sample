/**
 * Infrastructure support components.
 *
 * <p>This package contains framework-specific support components that are not pure configuration
 * classes but provide cross-cutting infrastructure behavior.
 *
 * <p><b>Contents:</b>
 * <ul>
 *   <li>{@link de.sample.aiarchitecture.infrastructure.support.AsyncInitializationProcessor}
 *       - Spring BeanPostProcessor for async initialization</li>
 * </ul>
 *
 * <p><b>Distinction from config package:</b>
 * <ul>
 *   <li>{@code config/} - Pure @Configuration classes with @Bean definitions</li>
 *   <li>{@code support/} - Framework components (processors, listeners, handlers)</li>
 * </ul>
 */
@org.jspecify.annotations.NullMarked
package de.sample.aiarchitecture.infrastructure.support;
