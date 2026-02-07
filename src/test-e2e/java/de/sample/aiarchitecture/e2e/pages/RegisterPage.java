package de.sample.aiarchitecture.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the registration page.
 *
 * <p>Provides methods to register new users.
 */
public class RegisterPage extends BasePage {

  private static final String URL_PATTERN = "/register";
  private static final String SUBMIT_BUTTON = "register-submit-button";
  private static final String CONFIRM_PASSWORD_INPUT = "register-confirm-password-input";

  /**
   * Creates a new RegisterPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public RegisterPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates to the register page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new RegisterPage
   */
  public static RegisterPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new RegisterPage(page);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public RegisterPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the password field.
   *
   * @param password the password
   * @return this page for method chaining
   */
  public RegisterPage fillPassword(String password) {
    fill("password", password);
    return this;
  }

  /**
   * Fills the confirm password field.
   *
   * @param password the password to confirm
   * @return this page for method chaining
   */
  public RegisterPage fillConfirmPassword(String password) {
    fill("confirmPassword", password);
    return this;
  }

  /**
   * Fills all registration fields.
   *
   * @param email the email address
   * @param password the password
   * @return this page for method chaining
   */
  public RegisterPage fillRegistrationForm(String email, String password) {
    return fillEmail(email)
        .fillPassword(password)
        .fillConfirmPassword(password);
  }

  /**
   * Submits the registration form.
   * After successful registration, caller should create the appropriate page object.
   */
  public void submit() {
    click(SUBMIT_BUTTON);
    page.waitForURL(url -> !url.contains("/register"));
  }

  /**
   * Performs registration with the given credentials.
   *
   * @param email the email address
   * @param password the password
   */
  public void register(String email, String password) {
    fillRegistrationForm(email, password);
    submit();
  }

  /**
   * Checks if confirm password field exists.
   *
   * @return true if confirm password is required
   */
  public boolean requiresConfirmPassword() {
    return exists(CONFIRM_PASSWORD_INPUT);
  }
}
