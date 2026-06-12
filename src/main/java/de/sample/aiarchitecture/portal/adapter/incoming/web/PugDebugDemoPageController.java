package de.sample.aiarchitecture.portal.adapter.incoming.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller that renders an intentionally broken Pug template.
 *
 * <p><b>Learning/demo artifact — not production code:</b> This controller demonstrates the Pug4j
 * debug error page provided by spring-pug4j ({@code Boot4PugDebugErrorViewResolver}, enabled via
 * {@code spring.pug4j.debug-error-page=true}). The referenced template contains an expression
 * error that fails at render time, so the resulting {@code PugException} propagates to Spring
 * Boot's error handling and is rendered as a debug page showing the template source with the
 * failing line highlighted.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/debug/broken.pug}
 */
@Controller
public class PugDebugDemoPageController {

  /**
   * Renders the intentionally broken template to trigger the Pug4j debug error page.
   *
   * @param model Spring MVC model to pass data to the view
   * @return view name "debug/broken" which resolves to templates/debug/broken.pug
   */
  @GetMapping("/debug/pug-error")
  public String showBrokenPage(final Model model) {
    model.addAttribute("title", "Pug4j Debug Error Page Test");

    return "debug/broken";
  }
}
