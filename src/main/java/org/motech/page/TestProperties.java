package org.motech.page;

import org.motech.exception.UITestFrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class TestProperties {

    private static final TestProperties SINGLETON = new TestProperties();

    public static final String WEBAPP_URL_PROPERTY = "webapp.url";

    public static final String DEFAULT_WEBAPP_URL = "http://localhost:8080/motech-platform-server/module/server";

    public static final String WEBDRIVER_PROPERTY = "webdriver";

    public static final String DEFAULT_WEBDRIVER = "chrome";

    public static final String LOGIN_PASSWORD_PROPERTY = "login.password";

    public static final String DEFAULT_PASSWORD = "motech";

    public static final String LOGIN_USERNAME_PROPERTY = "login.username";

    public static final String DEFAULT_LOGIN_USERNAME = "motech";

    public static TestProperties instance() {
        return SINGLETON;
    }

    private Properties properties;

    public TestProperties() {
        properties = new Properties();
        try {
            URL resource = Thread.currentThread().getContextClassLoader()
                    .getResource("org/motech/uitestframework/test.properties");
            if (resource != null) {
                try (InputStream input = resource.openStream()) {
                    properties.load(new InputStreamReader(input, "UTF-8"));
                }
            }
        } catch (IOException e) {
            throw new UITestFrameworkException("test.properties not found. Error: ", e);
        }
    }

    public String getWebAppUrl() {
        return getProperty(WEBAPP_URL_PROPERTY, DEFAULT_WEBAPP_URL);
    }

    public String getUserName() {
        return getProperty(LOGIN_USERNAME_PROPERTY, DEFAULT_LOGIN_USERNAME);
    }

    public String getPassword() {
        return getProperty(LOGIN_PASSWORD_PROPERTY, DEFAULT_PASSWORD);
    }

    public enum WebDriverType {
        chrome, firefox
    } // only these two for now

    public WebDriverType getWebDriver() {
        try {
            return WebDriverType.valueOf(getProperty(WEBDRIVER_PROPERTY, DEFAULT_WEBDRIVER));
        } catch (IllegalArgumentException e) {
            return WebDriverType.chrome;
        }
    }

    public String getProperty(String property, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(property);
        }
        if (value == null) {
            value = properties.getProperty(property);
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
}
