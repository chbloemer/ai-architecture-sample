package de.sample.aiarchitecture.sharedkernel.marker.port.in;

/**
 * Marker interface for Input Ports (Hexagonal Architecture).
 *
 * <p>Input ports are the entry points to the application layer. They define how the outside world
 * (driving/primary adapters) can interact with the application. In Hexagonal Architecture terms,
 * input ports sit on the "left side" of the hexagon.
 *
 * <p><b>Driving Adapters that use Input Ports:</b>
 * <ul>
 *   <li>REST Controllers</li>
 *   <li>GraphQL Resolvers</li>
 *   <li>CLI Command Handlers</li>
 *   <li>Event Consumers (external events)</li>
 *   <li>Scheduled Tasks</li>
 *   <li>MCP Tool Providers</li>
 * </ul>
 *
 * <p><b>Common Input Port Types:</b>
 * <ul>
 *   <li>{@link UseCase} - Command/Query pattern with INPUT and OUTPUT types</li>
 *   <li>Event Handlers - Process incoming events from other systems</li>
 * </ul>
 *
 * <p><b>Key Characteristics:</b>
 * <ul>
 *   <li>Technology-agnostic (no framework dependencies)</li>
 *   <li>Defined in terms of application/domain concepts</li>
 *   <li>Implemented by application layer classes (use cases)</li>
 *   <li>Called by adapters in the incoming adapter layer</li>
 * </ul>
 *
 * <p><b>Example Hierarchy:</b>
 * <pre>{@code
 * InputPort (marker)
 *   └── UseCase<INPUT, OUTPUT>
 *         └── CreateProductInputPort extends UseCase<CreateProductCommand, CreateProductResponse>
 * }</pre>
 *
 * @see UseCase
 * @see de.sample.aiarchitecture.sharedkernel.marker.port.out.OutputPort
 */
public interface InputPort {
  // Marker interface - no methods
}
