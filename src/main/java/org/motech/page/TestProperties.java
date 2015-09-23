package org.motech.page;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import java.net.URL;
/**
 * Created by tomasz on 23.09.15.
 */
public class TestProperties {

    private static TestProperties SINGLETON;

    public static final String WEBAPP_URL_PROPERTY = "webapp.url";

    public static final String DEFAULT_WEBAPP_URL = "http://localhost:8080/motech-platform-server/module/server";

    public static final String WEBDRIVER_PROPERTY = "webdriver";

    public static final String DEFAULT_WEBDRIVER = "chrome";

    public static final String LOGIN_PASSWORD_PROPERTY = "login.password";

    public static final String DEFAULT_PASSWORD = "motech";

    public static final String LOGIN_USERNAME_PROPERTY = "login.username";

    public static final String DEFAULT_LOGIN_USERNAME = "motech";

    public static TestProperties instance() {
        if (SINGLETON == null) {
            SINGLETON = new TestProperties();
        }
        return SINGLETON;
    }

    private Properties properties;

    public TestProperties() {
        properties = new Properties();
        try {
            URL resource = Thread.currentThread().getContextClassLoader()
                    .getResource("org/motech/uitestframework/test.properties");
            if (resource != null) {
                System.out.println("test.properties found: " + resource.toExternalForm());
                InputStream input = resource.openStream();
                properties.load(new InputStreamReader(input, "UTF-8"));
                System.out.println("test.properties:");
                System.out.println(properties);
            }
        }
        catch (IOException ioException) {
            throw new RuntimeException("test.properties not found. Error: ", ioException);
        }
        System.out.println(WEBAPP_URL_PROPERTY + ": " + getWebAppUrl());
        System.out.println(LOGIN_USERNAME_PROPERTY + ": " + getUserName());
        System.out.println(LOGIN_PASSWORD_PROPERTY + ": " + getPassword());
        System.out.println(WEBDRIVER_PROPERTY + ": " + getWebDriver());
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
    }; // only these two for now

    public WebDriverType getWebDriver() {
        try {
            return WebDriverType.valueOf(getProperty(WEBDRIVER_PROPERTY, DEFAULT_WEBDRIVER));
        }
        catch (IllegalArgumentException e) {
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
