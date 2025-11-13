package de.sample.aiarchitecture.sharedkernel.application.port;

/**
 * Marker interface for Input Ports (Hexagonal Architecture) / Use Cases (Clean Architecture).
 *
 * <p>An input port represents an entry point to the application layer, defining a single
 * use case or application feature. In Hexagonal Architecture, input ports are called by
 * primary/driving adapters (e.g., REST controllers, event consumers, CLI handlers).
 *
 * <p><b>Characteristics of Input Ports:</b>
 * <ul>
 *   <li>Represent a single user action or system operation</li>
 *   <li>Define the interface that use cases implement</li>
 *   <li>Accept Input models (Commands or Queries) as parameters</li>
 *   <li>Return Output models, not domain entities</li>
 *   <li>Technology-agnostic (no framework dependencies)</li>
 * </ul>
 *
 * <p><b>Input/Output Pattern:</b>
 * Input ports accept Input models and return Output models to decouple
 * the application layer from presentation and infrastructure concerns.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * public interface CreateProductCommandPort extends UseCase<CreateProductCommand, CreateProductResponse> {
 *     CreateProductResponse execute(CreateProductCommand input);
 * }
 *
 * public class CreateProductUseCase implements CreateProductCommandPort {
 *     @Override
 *     public CreateProductResponse execute(CreateProductCommand input) {
 *         // Use case implementation
 *     }
 * }
 * }</pre>
 *
 * <p><b>Naming Convention:</b>
 * Input ports should be named using the pattern: `{Action}{Entity}UseCase`
 * (e.g., CreateProductCommandPort, UpdateProductPriceCommandPort)
 *
 * <p><b>References:</b>
 * <ul>
 *   <li>Alistair Cockburn - Hexagonal Architecture (Ports & Adapters)</li>
 *   <li>Robert C. Martin - Clean Architecture (Chapter 19-20: Use Cases)</li>
 *   <li>Tom Hombergs - Get Your Hands Dirty on Clean Architecture</li>
 * </ul>
 *
 * @param <INPUT> the input model type (Command or Query)
 * @param <OUTPUT> the output model type (Response)
 */
public interface UseCase<INPUT, OUTPUT> {

  /**
   * Executes this use case with the given input.
   *
   * @param input the use case input (Command or Query)
   * @return the use case output (Response)
   */
  OUTPUT execute(INPUT input);
}
