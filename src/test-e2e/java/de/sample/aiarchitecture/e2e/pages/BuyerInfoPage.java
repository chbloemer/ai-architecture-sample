package de.sample.aiarchitecture.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the buyer information page in checkout.
 *
 * <p>Provides methods to fill buyer information and navigate to login/register
 * for authenticated checkout.
 */
public class BuyerInfoPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/buyer";
  private static final String CONTINUE_BUTTON = "buyer-continue-button";
  private static final String LOGIN_LINK = "buyer-login-link";
  private static final String REGISTER_LINK = "buyer-register-link";
  private static final String ERROR_MESSAGE = "buyer-error-message";

  /**
   * Creates a new BuyerInfoPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public BuyerInfoPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public BuyerInfoPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the first name field.
   *
   * @param firstName the first name
   * @return this page for method chaining
   */
  public BuyerInfoPage fillFirstName(String firstName) {
    fill("firstName", firstName);
    return this;
  }

  /**
   * Fills the last name field.
   *
   * @param lastName the last name
   * @return this page for method chaining
   */
  public BuyerInfoPage fillLastName(String lastName) {
    fill("lastName", lastName);
    return this;
  }

  /**
   * Fills the phone number field.
   *
   * @param phone the phone number
   * @return this page for method chaining
   */
  public BuyerInfoPage fillPhone(String phone) {
    fill("phone", phone);
    return this;
  }

  /**
   * Fills all buyer information fields at once.
   *
   * @param email the email address
   * @param firstName the first name
   * @param lastName the last name
   * @param phone the phone number
   * @return this page for method chaining
   */
  public BuyerInfoPage fillBuyerInfo(String email, String firstName, String lastName, String phone) {
    return fillEmail(email)
        .fillFirstName(firstName)
        .fillLastName(lastName)
        .fillPhone(phone);
  }

  /**
   * Clicks the continue button to proceed to delivery.
   *
   * @return the DeliveryPage
   */
  public DeliveryPage continueToDelivery() {
    click(CONTINUE_BUTTON);
    return new DeliveryPage(page);
  }

  /**
   * Clicks the continue button, expecting to stay on the same page due to validation errors.
   *
   * @return this page for method chaining
   */
  public BuyerInfoPage submitWithErrors() {
    click(CONTINUE_BUTTON);
    return this;
  }

  /**
   * Navigates to the login page from checkout.
   *
   * @return the LoginPage
   */
  public LoginPage goToLogin() {
    click(LOGIN_LINK);
    return new LoginPage(page);
  }

  /**
   * Navigates to the register page from checkout.
   *
   * @return the RegisterPage
   */
  public RegisterPage goToRegister() {
    click(REGISTER_LINK);
    return new RegisterPage(page);
  }

  /**
   * Checks if an error message is displayed.
   *
   * @return true if error message exists
   */
  public boolean hasErrorMessage() {
    return exists(ERROR_MESSAGE);
  }

  /**
   * Checks if the page contains validation error text.
   *
   * @return true if validation errors are shown
   */
  public boolean hasValidationErrors() {
    return hasErrorMessage() || pageContains("valid email") || pageContains("error");
  }

  /**
   * Checks if the current URL indicates we're still on the buyer info page.
   *
   * @return true if still on buyer info page
   */
  public boolean isOnPage() {
    return getCurrentPath().contains("/checkout/buyer");
  }
}
