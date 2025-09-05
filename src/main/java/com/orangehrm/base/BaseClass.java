package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.LoggerManager;

public class BaseClass {

    protected static Properties prop;
    protected static WebDriver driver;
    private static ActionDriver actionDriver;
    public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

    @BeforeSuite
    public void loadConfig() throws IOException {
        prop = new Properties();
        String path = System.getProperty("user.dir") + "/src/main/resources/config.properties";
        FileInputStream fis = new FileInputStream(path);
        prop.load(fis);
        logger.info("config.properties file loaded");
    }

    @BeforeMethod
    public void setup() throws IOException {
        System.out.println("Setting up WebDriver for:" + this.getClass().getSimpleName());
        launchBrowser();
        configureBrowser();
        staticWait(2);
        logger.info("WebDriver Initialized and Browser Maximized");

        if (actionDriver == null) {
            actionDriver = new ActionDriver(driver);
            System.out.println("ActionDriver instance is created.");
        }
    }

    private void launchBrowser() {
        String browser = prop.getProperty("browser", "chrome");

        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
            logger.info("ChromeDriver Instance is created");
        } else if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
            logger.info("FirefoxDriver Instance is created");
        } else if (browser.equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();
            logger.info("EdgeDriver Instance is created");
        } else {
            throw new IllegalArgumentException("Browser Not Supported: " + browser);
        }
    }

    private void configureBrowser() {
        String waitStr = prop.getProperty("implicitWait", "10");
        int implicitWait = Integer.parseInt(waitStr);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().window().maximize();

        try {
            driver.get(prop.getProperty("url_local"));
        } catch (Exception e) {
            System.out.println("Failed to Navigate to the Url:" + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Unable to quit the driver: " + e.getMessage());
            }
        }
        logger.info("WebDriver instance is closed.");
        driver = null;
        actionDriver = null;
    }

    public static Properties getProp() {
        return prop;
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static void setDriver(WebDriver driver) {
        BaseClass.driver = driver;
    }

    public static ActionDriver getActionDriver() {
        return actionDriver;
    }

    public void staticWait(int seconds) {
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }
}