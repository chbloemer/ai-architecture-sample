package de.sample.aiarchitecture.portadapter.incoming.web.product;

import de.sample.aiarchitecture.application.ProductApplicationService;
import de.sample.aiarchitecture.domain.model.product.Product;
import de.sample.aiarchitecture.domain.model.shared.ProductId;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for rendering product pages using Pug templates.
 *
 * <p>This controller handles traditional server-side rendered pages using Pug4j templates.
 * Unlike {@link ProductResource} which returns JSON for REST APIs, this controller
 * returns HTML views.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix. REST controllers use {@code @RestController} and end
 * with "Resource" suffix.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/product/}
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 *
 * @see ProductResource REST API controller
 */
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final ProductApplicationService productApplicationService;

  public ProductPageController(final ProductApplicationService productApplicationService) {
    this.productApplicationService = productApplicationService;
  }

  /**
   * Displays the product catalog page.
   *
   * <p>Returns a list of all products rendered using the Pug template.
   *
   * @param model Spring MVC model to pass data to the view
   * @return view name "product/catalog" which resolves to templates/product/catalog.pug
   */
  @GetMapping
  public String showProductCatalog(final Model model) {
    final List<Product> products = productApplicationService.getAllProducts();

    model.addAttribute("products", products);
    model.addAttribute("title", "Product Catalog");
    model.addAttribute("totalProducts", products.size());

    return "product/catalog";
  }

  /**
   * Displays a single product detail page.
   *
   * <p>Shows detailed information about a specific product.
   *
   * @param id the product ID
   * @param model Spring MVC model to pass data to the view
   * @return view name "product/detail" or "error/404" if product not found
   */
  @GetMapping("/{id}")
  public String showProductDetail(@PathVariable final String id, final Model model) {
    return productApplicationService
        .findProductById(ProductId.of(id))
        .map(
            product -> {
              model.addAttribute("product", product);
              model.addAttribute("title", product.name().value());
              model.addAttribute("isAvailable", product.isAvailable());
              return "product/detail";
            })
        .orElse("error/404");
  }

  /**
   * Displays a sample landing page demonstrating Pug4j features.
   *
   * @param model Spring MVC model
   * @return view name "product/sample"
   */
  @GetMapping("/sample")
  public String showSamplePage(final Model model) {
    model.addAttribute("title", "Pug4j Sample Page");
    model.addAttribute("message", "Welcome to the Product Catalog");
    model.addAttribute("features", List.of("DDD Architecture", "Clean Code", "Pug Templates"));

    return "product/sample";
  }
}
