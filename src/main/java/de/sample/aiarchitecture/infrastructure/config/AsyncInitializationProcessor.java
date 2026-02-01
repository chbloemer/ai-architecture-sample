package de.sample.aiarchitecture.infrastructure.config;

import de.sample.aiarchitecture.sharedkernel.marker.infrastructure.AsyncInitialize;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * Infrastructure processor that IMPLEMENTS the behavior for {@link AsyncInitialize}.
 *
 * <p>This is where the framework-specific code lives (Spring BeanPostProcessor).
 * The annotation itself (in sharedkernel) is framework-agnostic.
 *
 * <p><b>Separation of Concerns:</b>
 * <ul>
 *   <li>Annotation definition: {@code sharedkernel.marker.infrastructure} (pure Java)</li>
 *   <li>Annotation processing: {@code infrastructure.config} (Spring-specific)</li>
 * </ul>
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>After a bean is initialized, this processor checks for {@link AsyncInitialize} annotation</li>
 *   <li>If found, looks for a method named {@code asyncInitialize()} with no parameters</li>
 *   <li>Invokes the method (should be marked with {@code @Async} for async execution)</li>
 *   <li>Logs any errors without breaking application startup</li>
 * </ol>
 *
 * <p><b>Important:</b> The {@code asyncInitialize()} method should be annotated with
 * {@code @Async} to ensure true asynchronous execution. Without {@code @Async}, the method
 * will execute synchronously during bean post-processing.
 *
 * @see AsyncInitialize
 * @see AsyncConfiguration
 */
@Component
public class AsyncInitializationProcessor implements BeanPostProcessor, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(AsyncInitializationProcessor.class);
  private static final String INIT_METHOD_NAME = "asyncInitialize";

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {

    // Check if bean is annotated with @AsyncInitialize
    AsyncInitialize annotation =
        AnnotationUtils.findAnnotation(bean.getClass(), AsyncInitialize.class);

    if (annotation != null) {
      logger.debug(
          "Found @AsyncInitialize on bean '{}' with priority {} and description '{}'",
          beanName,
          annotation.priority(),
          annotation.description());

      try {
        // Find asyncInitialize() method
        Method initMethod = bean.getClass().getMethod(INIT_METHOD_NAME);

        // Log initialization start
        if (!annotation.description().isEmpty()) {
          logger.info(
              "Triggering async initialization for '{}': {}", beanName, annotation.description());
        } else {
          logger.info("Triggering async initialization for '{}'", beanName);
        }

        // Invoke the method (should be @Async for true async execution)
        initMethod.invoke(bean);

      } catch (NoSuchMethodException e) {
        logger.warn(
            "Bean '{}' is annotated with @AsyncInitialize but has no {}() method. "
                + "Add a public void {}() method annotated with @Async.",
            beanName,
            INIT_METHOD_NAME,
            INIT_METHOD_NAME);
      } catch (Exception e) {
        // Log error but don't break application startup
        logger.error(
            "Error during async initialization of '{}': {}. Application continues to start.",
            beanName,
            e.getMessage(),
            e);
      }
    }

    return bean;
  }

  @Override
  public int getOrder() {
    // Run after most other post-processors to ensure bean is fully initialized
    return Ordered.LOWEST_PRECEDENCE;
  }
}
