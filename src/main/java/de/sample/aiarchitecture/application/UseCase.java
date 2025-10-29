package de.sample.aiarchitecture.application;

/**
 * Marker interface for Use Cases (Clean Architecture).
 *
 * <p>A use case represents a single application feature or user interaction.
 * In Clean Architecture, use cases are also known as "interactors" or "input ports"
 * in Hexagonal Architecture.
 *
 * <p><b>Characteristics of Use Cases:</b>
 * <ul>
 *   <li>Represent a single user action or system operation</li>
 *   <li>Orchestrate domain objects to fulfill a business requirement</li>
 *   <li>Define transactional boundaries</li>
 *   <li>Publish domain events after successful execution</li>
 *   <li>Return Output models, not domain entities</li>
 * </ul>
 *
 * <p><b>Input/Output Pattern:</b>
 * Use cases accept Input models and return Output models to decouple
 * the application layer from presentation and infrastructure concerns.
 *
 * <p><b>Example:</b>
 * <pre>{@code
 * public interface CreateProductUseCase extends UseCase<CreateProductInput, CreateProductOutput> {
 *     CreateProductOutput execute(CreateProductInput input);
 * }
 * }</pre>
 *
 * <p><b>Interface Segregation:</b>
 * Each use case should be a separate interface (not a service with many methods).
 * This follows the Interface Segregation Principle and allows fine-grained dependencies.
 *
 * <p><b>References:</b>
 * <ul>
 *   <li>Robert C. Martin - Clean Architecture (Chapter 19-20: Use Cases)</li>
 *   <li>Tom Hombergs - Get Your Hands Dirty on Clean Architecture</li>
 * </ul>
 *
 * @param <I> the input model type
 * @param <O> the output model type
 */
public interface UseCase<I, O> {

  /**
   * Executes this use case with the given input.
   *
   * @param input the use case input
   * @return the use case output
   */
  O execute(I input);
}
