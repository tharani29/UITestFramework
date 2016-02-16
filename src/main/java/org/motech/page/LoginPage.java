package org.motech.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.lang.InterruptedException;
import java.lang.Override;
import java.lang.String;

public class LoginPage extends AbstractBasePage {

    private static final By USERNAME = By.name("j_username");
    private static final By PASSWORD = By.name("j_password");
    private static final By LOGIN = By.cssSelector("input.btn.btn-primary");

    public static final String LOGIN_PATH = "/module/server/login";

    private final String adminUsername;
    private final String adminPassword;

    public LoginPage(WebDriver driver) {
        super(driver);
        adminUsername = getProperties().getUserName();
        adminPassword = getProperties().getPassword();
    }

    public void logOut() throws InterruptedException {
        waitForElement(By.cssSelector("span.ng-binding"));
        clickWhenVisible(By.cssSelector("span.ng-binding"));
        clickOn(By.xpath("//a[@href='j_spring_security_logout']"));
    }

    public void login(String user, String password) {
        waitForElement(USERNAME);
        setTextToFieldNoEnter(USERNAME, user);
        setTextToFieldNoEnter(PASSWORD, password);
        clickOn(LOGIN);
        waitForElement(By.cssSelector("span.ng-binding"));
    }

    public void loginAsAdmin() {
        login(adminUsername, adminPassword);
    }

    @Override
    public String expectedUrlPath() {
        return LOGIN_PATH;
    }

    @Override
    public void goToPage() {
        getDriver().get(getMotechUrl() + LOGIN_PATH);
    }
}
