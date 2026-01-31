package de.sample.aiarchitecture.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the order review page in checkout.
 *
 * <p>Provides methods to verify order details and place the order.
 */
public class ReviewPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/review";
  private static final String PLACE_ORDER_BUTTON = "review-place-order-button";

  /**
   * Creates a new ReviewPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public ReviewPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Checks if the page displays the expected email.
   *
   * @param email the expected email address
   * @return true if the email is displayed
   */
  public boolean showsEmail(String email) {
    return pageContains(email);
  }

  /**
   * Checks if the page displays the expected address.
   *
   * @param address the expected address text
   * @return true if the address is displayed
   */
  public boolean showsAddress(String address) {
    return pageContains(address);
  }

  /**
   * Clicks the place order button to complete the purchase.
   *
   * @return the ConfirmationPage
   */
  public ConfirmationPage placeOrder() {
    click(PLACE_ORDER_BUTTON);
    return new ConfirmationPage(page);
  }
}
