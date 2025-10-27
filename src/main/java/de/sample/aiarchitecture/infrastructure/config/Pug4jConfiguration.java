package de.sample.aiarchitecture.infrastructure.config;

import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.spring.template.SpringTemplateLoader;
import de.neuland.pug4j.spring.view.PugViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;

/**
 * Configuration for Pug4j template engine.
 *
 * <p>Pug4j is a Java implementation of the Pug template engine (formerly Jade).
 * This configuration sets up Pug4j to work with Spring MVC for server-side rendering.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/}
 * <p><b>File Extension:</b> {@code .pug}
 * <p><b>View Resolution:</b> Pug templates are resolved before other view resolvers
 *
 * @see <a href="https://github.com/neuland/pug4j">Pug4j on GitHub</a>
 */
@Configuration
public class Pug4jConfiguration {

  /**
   * Configures the Pug template loader.
   *
   * @return configured template loader pointing to classpath:templates/
   */
  @Bean
  public SpringTemplateLoader templateLoader() {
    final SpringTemplateLoader templateLoader = new SpringTemplateLoader();
    templateLoader.setTemplateLoaderPath("classpath:/templates/");
    templateLoader.setSuffix(".pug");
    return templateLoader;
  }

  /**
   * Configures the Pug configuration with caching and pretty-print settings.
   *
   * @param templateLoader the template loader
   * @return configured PugConfiguration
   */
  @Bean
  public PugConfiguration pugConfiguration(final SpringTemplateLoader templateLoader) {
    final PugConfiguration configuration = new PugConfiguration();
    configuration.setTemplateLoader(templateLoader);
    configuration.setCaching(false); // Disable for development (enable in production)
    configuration.setPrettyPrint(true); // Pretty-print HTML output
    return configuration;
  }

  /**
   * Configures the Pug view resolver for Spring MVC.
   *
   * <p>This resolver handles views returned by @Controller methods.
   * It has order 0 to be processed before other view resolvers.
   *
   * @param pugConfiguration the Pug configuration
   * @return configured view resolver
   */
  @Bean
  public ViewResolver pugViewResolver(final PugConfiguration pugConfiguration) {
    final PugViewResolver viewResolver = new PugViewResolver();
    viewResolver.setConfiguration(pugConfiguration);
    viewResolver.setOrder(0); // Process Pug templates first
    return viewResolver;
  }
}
