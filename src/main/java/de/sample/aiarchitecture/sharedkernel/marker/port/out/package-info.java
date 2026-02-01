/**
 * Output Ports (Driven/Secondary Ports).
 *
 * <p>Output ports define what the application needs from the outside world.
 * They represent dependencies that the application layer requires but does
 * not implement itself.
 *
 * <p><b>Driven Adapters that implement Output Ports:</b>
 * <ul>
 *   <li>Repository implementations (database access)</li>
 *   <li>External API clients (REST, gRPC)</li>
 *   <li>Message publishers (Kafka, RabbitMQ)</li>
 *   <li>Email/SMS services</li>
 *   <li>File storage services</li>
 * </ul>
 *
 * <p><b>Port Hierarchy:</b>
 * <pre>
 * OutputPort (marker)
 *   ├── Repository&lt;T, ID&gt;
 *   ├── DomainEventPublisher
 *   └── IdentityProvider
 * </pre>
 */
package de.sample.aiarchitecture.sharedkernel.marker.port.out;
