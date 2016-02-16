package org.motech.test;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.motech.exception.UITestFrameworkException;
import org.motech.page.GenericPage;
import org.motech.page.LoginPage;
import org.motech.page.Page;
import org.motech.page.TestProperties;
import org.motech.startup.StartupHelper;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
/**
 * Superclass for all UI Tests. Contains lots of handy "utilities"
 * needed to setup and tear down tests as well as handy methods
 * needed during tests, such as:
 *  - initialize Selenium WebDriver
 *  - @see {@link #currentPage()}
 *  - @see {@link #assertPage(Page)}
 */
public class TestBase {

    private static final TestProperties TEST_PROPERTIES = TestProperties.instance();
    private static final String SERVER_URL = TEST_PROPERTIES.getWebAppUrl();

    private static WebDriver driver;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LoginPage loginPage;

    @BeforeClass
    public static void startWebDriver() throws InterruptedException, IOException {
        driver = setupChromeDriver();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        if (SERVER_URL.equals(TestProperties.DEFAULT_WEBAPP_URL)) {
            new StartupHelper().startUp();
        }
        goToHomePage();
    }

    @AfterClass
    public static void stopWebDriver() {
        driver.quit();
    }

    @Before
    public void initLoginPage() {
        loginPage = new LoginPage(driver);
    }

    public void login() {
        assertPage(loginPage);
        loginPage.loginAsAdmin();
    }

    public void logout() throws InterruptedException {
        loginPage.logOut();
    }

    public static void goToHomePage() {
        currentPage().gotoPage("/home");
    }

    // This takes a screen (well, browser) snapshot whenever there's a failure
    // and stores it in a "screenshots" directory.
    @Rule
    public TestRule testWatcher = new TestWatcher() { // NO CHECKSTYLE rules must be public

        @Override
        public void failed(Throwable t, Description test) {
            takeScreenshot(test.getDisplayName().replaceAll("[()]", ""));
        }
    };

    static WebDriver setupChromeDriver() {
        URL chromedriverExecutable = null;
        ClassLoader classLoader = TestBase.class.getClassLoader();

        String chromedriverExecutableFilename = null;
        String chromedriverExecutablePath;

        if (SystemUtils.IS_OS_MAC_OSX) {
            chromedriverExecutableFilename = "chromedriver";
            chromedriverExecutable = classLoader.getResource("chromedriver/mac/chromedriver");
        } else if (SystemUtils.IS_OS_LINUX) {
            chromedriverExecutableFilename = "chromedriver";
            chromedriverExecutable = classLoader.getResource("chromedriver/linux/chromedriver");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            chromedriverExecutableFilename = "chromedriver.exe";
            chromedriverExecutable = classLoader.getResource("chromedriver/windows/chromedriver.exe");
        }

        if (chromedriverExecutable == null) {
            throw new UITestFrameworkException("Cannot find chromedriver executable");
        } else {
            chromedriverExecutablePath = chromedriverExecutable.getPath();
            // This ugly bit checks to see if the chromedriver file is inside a jar, and if so
            // uses VFS to extract it to a temp directory.
            if (chromedriverExecutablePath.contains(".jar!")) {
                FileObject chromedriverVfs;
                try {
                    chromedriverVfs = VFS.getManager().resolveFile(chromedriverExecutable.toExternalForm());
                    File chromedriverFs = new File(FileUtils.getTempDirectory(), chromedriverExecutableFilename);
                    FileObject chromedriverUnzipped = VFS.getManager().toFileObject(chromedriverFs);
                    chromedriverUnzipped.delete();
                    chromedriverUnzipped.copyFrom(chromedriverVfs, new AllFileSelector());
                    chromedriverExecutablePath = chromedriverFs.getPath();
                    if (!SystemUtils.IS_OS_WINDOWS) {
                        chromedriverFs.setExecutable(true);
                    }
                } catch (FileSystemException e) {
                    throw new UITestFrameworkException("Unable to start the UI Test Framework", e);
                }
            }
        }
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromedriverExecutablePath);
        String chromedriverFilesDir = "target/chromedriverlogs";
        try {
            FileUtils.forceMkdir(new File(chromedriverFilesDir));
        } catch (IOException e) {
            throw new UITestFrameworkException("Unable to start the UI Test Framework", e);
        }
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, chromedriverFilesDir + "/chromedriver-"
                + testClassName.name + ".log");
        driver = new ChromeDriver();
        return driver;
    }

    /**
     * Return a Page that represents the current page, so that all the convenient methods in Page
     * can be used.
     *
     * @return a Page
     */
    public static Page currentPage() {
        return new GenericPage(driver);
    }

    /**
     * Assert we're on the expected page.
     *
     * @param expected page
     */
    public static void assertPage(Page expected) {
        assertEquals(expected.expectedUrlPath(), currentPage().urlPath());
    }

    public void takeScreenshot(String filename) {
        File tempFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(tempFile, new File("target/screenshots/" + filename + ".png"));
        } catch (IOException e) {
            throw new UITestFrameworkException("Unable to take screenshot", e);
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    protected static WebDriver getDriver() {
        return driver;
    }

    protected static TestProperties getTestProperties() {
        return TEST_PROPERTIES;
    }

    protected static String getServerUrl() {
        return SERVER_URL;
    }

    // This junit cleverness picks up the name of the test class, to be used in the chromedriver log file name.
    @ClassRule
    public static TestClassName testClassName = new TestClassName(); // NO CHECKSTYLE rules must be public

    static class TestClassName implements TestRule {

        private String name;

        @Override
        public Statement apply(Statement statement, Description description) {
            name = description.getTestClass().getSimpleName();
            return statement;
        }
    }
}

