/**
 * Input Ports (Driving/Primary Ports).
 *
 * <p>Input ports are the entry points to the application layer. They define how
 * the outside world (driving adapters) can interact with the application.
 *
 * <p><b>Driving Adapters that use Input Ports:</b>
 * <ul>
 *   <li>REST Controllers</li>
 *   <li>GraphQL Resolvers</li>
 *   <li>CLI Command Handlers</li>
 *   <li>Event Consumers</li>
 *   <li>Scheduled Tasks</li>
 *   <li>MCP Tool Providers</li>
 * </ul>
 *
 * <p><b>Port Hierarchy:</b>
 * <pre>
 * InputPort (marker)
 *   └── UseCase&lt;INPUT, OUTPUT&gt;
 *         └── *InputPort extends UseCase&lt;Command, Response&gt;
 * </pre>
 */
package de.sample.aiarchitecture.sharedkernel.marker.port.in;
