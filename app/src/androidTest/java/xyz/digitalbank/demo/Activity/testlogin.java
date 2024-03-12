package xyz.digitalbank.demo.Activity;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import io.appium.java_client.android.AndroidDriver;
import com.perfecto.reportium.client.ReportiumClient;
import org.openqa.selenium.remote.DesiredCapabilities;

public class testlogin {
    WebDriver driver;
    ReportiumClient reportiumClient;

    @Test
    public void appiumTest() throws Exception {
        // Set cloudName and securityToken
        String cloudName = "demo";
        String securityToken = "Gg";

        // Set repositoryKey and localFilePath
        String repositoryKey = "PUBLIC:Digital-Bank-1.4.apk";
        String localFilePath = System.getProperty("user.dir") + "/app/build/outputs/apk/debug/Digital-Bank-1.4.apk";

        // Set capabilities
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("model", "Google Pixel 6");
        capabilities.setCapability("enableAppiumBehavior", true);
        capabilities.setCapability("openDeviceTimeout", 2);
        capabilities.setCapability("app", repositoryKey);
        capabilities.setCapability("appPackage", "xyz.digitalbank.demo");
        capabilities.setCapability("autoLaunch", true);
        capabilities.setCapability("takesScreenshot", false);
        capabilities.setCapability("screenshotOnError", true);
        capabilities.setCapability("securityToken", securityToken);

        // Initialize driver
        driver = new AndroidDriver<>(new URL("https://" + cloudName + ".perfectomobile.com/nexperience/perfectomobile/wd/hub"), capabilities);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        // Your test steps
        driver.findElement(By.id("emailInput")).sendKeys("jsmith@demo.io");
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.presenceOfElementLocated(By.id("passwordInput")));
        driver.findElement(By.id("passwordInput")).sendKeys("Demo123!");
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.presenceOfElementLocated(By.id("loginButton")));
        driver.findElement(By.id("loginButton")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='500.0' and (preceding-sibling::* or following-sibling::*)[@text='1933.72']]")));
        driver.findElement(By.xpath("//*[@text='500.0' and (preceding-sibling::* or following-sibling::*)[@text='1933.72']]")).click();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(By.id("checkbox1")));
        driver.findElement(By.id("checkbox1")).click();
        driver.findElement(By.id("getLocationButton")).click();
        driver.findElement(By.id("logoutButton")).click();
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        // Your after method code here
        if (driver != null) {
            driver.quit();
        }
    }
}
