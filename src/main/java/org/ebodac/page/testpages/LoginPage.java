package org.ebodac.page.testpages;


import org.ebodac.page.AbstractBasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends AbstractBasePage {

    public static final String SUPERUSER = "motech";
    public static final String SUPERPASSWORD = "Ebodac!2";
    static final By USERNAME = By.name("j_username");
    static final By PASSWORD = By.name("j_password");
    static final By LOGIN = By.cssSelector("input.btn.btn-primary");
    public static final String LOGIN_PATH = "/login.htm";
    static final String LOGOUT_PATH = "/logout";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void logOut() throws InterruptedException{

        findElement(By.cssSelector("span.ng-binding")).click();
        findElement(By.xpath("//a[@href='j_spring_security_logout']")).click();
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
        login(SUPERUSER, SUPERPASSWORD);
    }

    @Override
    public String expectedUrlPath() {
        return URL_ROOT + LOGIN_PATH;
    }
}
