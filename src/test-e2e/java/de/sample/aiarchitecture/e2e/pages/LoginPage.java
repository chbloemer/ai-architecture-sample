package de.sample.aiarchitecture.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the login page.
 *
 * <p>Provides methods to authenticate users.
 */
public class LoginPage extends BasePage {

  private static final String URL_PATTERN = "/login";
  private static final String SUBMIT_BUTTON = "login-submit-button";
  private static final String REGISTER_LINK = "login-register-link";

  /**
   * Creates a new LoginPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public LoginPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates to the login page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new LoginPage
   */
  public static LoginPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new LoginPage(page);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public LoginPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the password field.
   *
   * @param password the password
   * @return this page for method chaining
   */
  public LoginPage fillPassword(String password) {
    fill("password", password);
    return this;
  }

  /**
   * Fills login credentials.
   *
   * @param email the email address
   * @param password the password
   * @return this page for method chaining
   */
  public LoginPage fillCredentials(String email, String password) {
    return fillEmail(email).fillPassword(password);
  }

  /**
   * Submits the login form.
   * After successful login, caller should create the appropriate page object.
   */
  public void submit() {
    click(SUBMIT_BUTTON);
    waitForTimeout(1000);
  }

  /**
   * Performs login with the given credentials.
   *
   * @param email the email address
   * @param password the password
   */
  public void login(String email, String password) {
    fillCredentials(email, password);
    submit();
  }

  /**
   * Navigates to the registration page.
   *
   * @return the RegisterPage
   */
  public RegisterPage goToRegister() {
    click(REGISTER_LINK);
    return new RegisterPage(page);
  }

  /**
   * Checks if the register link exists.
   *
   * @return true if register link is available
   */
  public boolean hasRegisterLink() {
    return exists(REGISTER_LINK);
  }
}
