package org.motech.page;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;import java.lang.InterruptedException;import java.lang.Override;import java.lang.String;

public class LoginPage extends AbstractBasePage {

    private String UserName;
    private String Password;
    static final By USERNAME = By.name("j_username");
    static final By PASSWORD = By.name("j_password");
    static final By LOGIN = By.cssSelector("input.btn.btn-primary");
    public static final String LOGIN_PATH = "/login.htm";
    static final String LOGOUT_PATH = "/logout";

    public LoginPage(WebDriver driver) {
        super(driver);
        UserName = properties.getUserName();
        Password = properties.getPassword();
    }

    public void logOut() throws InterruptedException{
        waitForElement(By.cssSelector("span.ng-binding"));
        clickWhenVisible(By.cssSelector("span.ng-binding"));
        clickOn(By.xpath("//a[@href='j_spring_security_logout']"));
    }

    public void login(String user, String password, int location) {
        waitForElement(USERNAME);
        setTextToFieldNoEnter(USERNAME, user);
        setTextToFieldNoEnter(PASSWORD, password);
        clickOn(LOGIN);
        waitForElement(By.cssSelector("span.ng-binding"));
    }

    public void login(String user, String password) {
        login(user, password, 0);
    }

    public void loginAsAdmin() {
        login(UserName, Password);
    }

    @Override
    public String expectedUrlPath() {
        return URL_ROOT + LOGIN_PATH;
    }
}
