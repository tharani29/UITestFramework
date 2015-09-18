package org.ebodac.test.automatictests;

import org.ebodac.page.testpages.LoginPage;
import org.ebodac.test.TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tomasz on 16.09.15.
 */
public class LoginLogoutTest extends TestBase {
    private LoginPage loginPage;

    @Before
    public void setUp() {
        loginPage = new LoginPage(driver);
        login();
    }

    @Test
    public void loginLogoutTest() {

    }

    @After
    public void tearDown() throws Exception {
        loginPage.logOut();
    }
}
