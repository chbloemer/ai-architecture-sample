package de.sample.aiarchitecture.product.adapter.incoming.web;

import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsQuery;
import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsResult;
import de.sample.aiarchitecture.product.application.getallproducts.GetAllProductsUseCase;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdQuery;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdResult;
import de.sample.aiarchitecture.product.application.getproductbyid.GetProductByIdUseCase;
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
 * Unlike REST API controllers which return JSON, this controller returns HTML views.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and
 * end with "Controller" suffix. REST controllers use {@code @RestController} and end
 * with "Resource" suffix.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/product/}
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 */
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final GetAllProductsUseCase getAllProductsUseCase;
  private final GetProductByIdUseCase getProductByIdUseCase;

  public ProductPageController(
      final GetAllProductsUseCase getAllProductsUseCase,
      final GetProductByIdUseCase getProductByIdUseCase) {
    this.getAllProductsUseCase = getAllProductsUseCase;
    this.getProductByIdUseCase = getProductByIdUseCase;
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
    final GetAllProductsResult output = getAllProductsUseCase.execute(new GetAllProductsQuery());

    final List<GetAllProductsResult.ProductSummary> products = output.products();

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
    final GetProductByIdResult output = getProductByIdUseCase.execute(new GetProductByIdQuery(id));

    if (!output.found()) {
      return "error/404";
    }

    model.addAttribute("product", output);
    model.addAttribute("title", output.name());
    // Note: isAvailable should be obtained from Inventory context

    return "product/detail";
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
