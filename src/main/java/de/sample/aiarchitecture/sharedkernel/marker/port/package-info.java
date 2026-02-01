/**
 * Hexagonal Architecture Port Interfaces.
 *
 * <p>This package contains the port interfaces for Hexagonal (Ports and Adapters) Architecture:
 * <ul>
 *   <li>{@code in/} - Input ports (driving/primary) - entry points to the application</li>
 *   <li>{@code out/} - Output ports (driven/secondary) - dependencies the application needs</li>
 * </ul>
 *
 * <p><b>Architecture:</b>
 * <pre>
 *   [Driving Adapters] → [Input Ports] → [Application] → [Output Ports] → [Driven Adapters]
 *   (REST, Web, CLI)      (UseCase)        (Domain)      (Repository)     (DB, APIs, MQ)
 * </pre>
 *
 * <p><b>References:</b>
 * <ul>
 *   <li>Alistair Cockburn - Hexagonal Architecture (Ports & Adapters)</li>
 *   <li>Robert C. Martin - Clean Architecture</li>
 * </ul>
 */
package de.sample.aiarchitecture.sharedkernel.marker.port;
