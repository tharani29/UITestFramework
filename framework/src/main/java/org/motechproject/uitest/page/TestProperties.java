package org.motechproject.uitest.page;

import org.motechproject.uitest.exception.UITestFrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class TestProperties {

    private static final TestProperties SINGLETON = new TestProperties();

    public static final String SERVER_URL_PROPERTY = "server.url";

    public static final String DEFAULT_SERVER_URL = "http://localhost:8080";

    public static final String CONTEXT_PATH_PROPERTY = "context_path";

    public static final String DEFAULT_CONTEXT_PATH = "/motech-platform-server";

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
                    .getResource("ui-test.properties");
            if (resource != null) {
                try (InputStream input = resource.openStream()) {
                    properties.load(new InputStreamReader(input, "UTF-8"));
                }
            }
        } catch (IOException e) {
            throw new UITestFrameworkException("Error reading test properties", e);
        }
    }

    public String getContextPath() {
        return getProperty(CONTEXT_PATH_PROPERTY, DEFAULT_CONTEXT_PATH);
    }

    public String getServerUrl() {
        return getProperty(SERVER_URL_PROPERTY, DEFAULT_SERVER_URL);
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
