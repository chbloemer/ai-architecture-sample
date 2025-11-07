package de.sample.aiarchitecture.portal.adapter.incoming.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MVC Controller for the application home/landing page.
 *
 * <p>This controller handles the root URL ("/") and displays a landing page
 * with navigation to the main features of the application.
 *
 * <p><b>Bounded Context:</b> Portal - Entry point providing navigation to
 * Product Catalog, Shopping Cart, and other application features.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/home/index.pug}
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 */
@Controller
public class HomePageController {

  /**
   * Displays the application landing page.
   *
   * <p>This page serves as the entry point to the application, providing
   * links to:
   * <ul>
   *   <li>Product Catalog (web UI)</li>
   *   <li>Shopping Cart (web UI)</li>
   *   <li>REST API endpoints</li>
   *   <li>Architecture documentation</li>
   * </ul>
   *
   * @param model Spring MVC model to pass data to the view
   * @return view name "home/index" which resolves to templates/home/index.pug
   */
  @GetMapping("/")
  public String showHomePage(final Model model) {
    model.addAttribute("title", "AI Architecture Sample");
    model.addAttribute("subtitle", "DDD, Hexagonal & Onion Architecture Demo");

    return "home/index";
  }
}